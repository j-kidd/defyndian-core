package defyndian.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import defyndian.config.DefyndianConfig;
import defyndian.config.MysqlConfig;
import defyndian.config.RabbitMQDetails;
import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.exception.MalformedConfigFileException;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import defyndian.messaging.BasicDefyndianMessage;
import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.DefyndianRoutingType;
import defyndian.messaging.InvalidRoutingKeyException;
import defyndian.messaging.RoutingInfo;
import defyndian.messaging.SystemPresenceMessage;

/**
 * This is the base class for all components of the system, it contains the Inbox/Outbox 
 * structures and the ability to start a publisher or consumer. It manages the MQ and DB
 * connections, logger and config.
 * @author james
 *
 */
public abstract class DefyndianNode implements AutoCloseable{

	private static final String BASE_LOGGER_NAME = "Defyndian";
	private static final String STATION_NAME = "Station";
	private static final int MAX_INBOX_SIZE = 10;
	private static final int MAX_OUTBOX_SIZE = 5;
	private static final long TIMEOUT_SECONDS = 5;
	
	private Connection mqConnection;
	private java.sql.Connection dbConnection;
	protected DefyndianConfig config;
	
	protected final Logger logger;
	private boolean STOP = false;
	private LinkedBlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> inbox;
	private LinkedBlockingQueue<DefyndianEnvelope<? extends DefyndianMessage>> outbox;
	protected final Publisher publisher;
	protected final Consumer consumer;
	
	private final String name;
	private final String host;
	
	// A node is in Brigade if it is in contact with a DefyndianStation on this network
	private boolean inBrigade;
	
	/**
	 * The constructor initialises all managed resources and declares the
	 * queues/exchanges from the config
	 * @param name
	 * @throws DefyndianMQException If the MQConnection could not be initialised
	 * @throws DefyndianDatabaseException If the DBConnection could not be initialised
	 * @throws ConfigInitialisationException 
	 */
	public DefyndianNode(String name) throws DefyndianMQException, DefyndianDatabaseException, ConfigInitialisationException{
		this.name = name;
		logger = LogManager.getLogger(BASE_LOGGER_NAME+"."+name);
		try{
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e ){
			throw new IllegalStateException("Cannot fail to recognise local host");
		}
		config = initialiseConfig();
		RabbitMQDetails rmqDetails = config.getRabbitMQDetails();
		mqConnection = initialiseMQConnection(rmqDetails);
		dbConnection = initialiseDBConnection(config.getDataSource());
		initialiseInboxOutbox();
		try {
			consumer = new Consumer(inbox, mqConnection.createChannel(), getName(), rmqDetails, config.getRoutingKeys());
			consumer.start(host + "-" + getName());
			publisher = new Publisher(outbox, mqConnection.createChannel());
		} catch (IOException e) {
			throw new DefyndianMQException("Could not create new channel in creation of new Node " + name);
		}
		try{
			inBrigade = contactStation();
		} catch (InterruptedException e){
			inBrigade = false;
		}
	}
	
	/**
	 * Main method used to decide if a node should exit, it checks for the 
	 * STOP value being set and then uses the overridable shouldExit method
	 * to ultimately determine if exit is necessary
	 * @return True if should exit
	 */
	protected final boolean topShouldExit(){
		if( STOP==true )
			return true;
		else
			return shouldExit();
	}
	
	/**
	 * Provided method to allow subclasses to setup additional resources before the 
	 * start method is called, default is to log that no setup was performed
	 */
	protected void setup(){
		logger.info("No setup specified, using default");
	}
	
	/**
	 * Overidable method used to implement additional exit conditions beyond the STOP
	 * value being set, defaults to false meaning the STOP mechanism is solely responsible
	 * for stopping the node.
	 * @return True if the node should exit
	 */
	protected boolean shouldExit(){
		return false;
	}
	
	/**
	 * Abstract method used to implement the processing of a particular node
	 * @throws Exception
	 */
	public abstract void start() throws Exception;
	
	
	/**
	 * Used to stop the processing of this node, will shutdown a published if one is present
	 * meaning no messages are delivered to the system from this node.
	 */
	public final void stop(){
		if( publisher!=null )
			publisher.setStop();
	}
	
	/**
	 * Similar to stop but cannot be continued, used to ultimately stop and shutdown this node
	 */
	public final void close(){
		if( publisher!=null ){
			publisher.setStop();
		}
		try{
			mqConnection.close();
		} catch( IOException e ){
			logger.error("Could not shutdown mq connection: " + e);
		}
		
		try {
			dbConnection.close();
		} catch (SQLException e) {
			logger.error("Could not shutdown db connection: " + e);
		}
	}
	
	public String getName(){
		return name;
	}
	
	public static final String getStationName(){
		return STATION_NAME;
	}
	
	protected Connection getMQConnection(){
		return mqConnection;
	}
	
	protected java.sql.Connection getDBConnection(){
		return dbConnection;
	}
	
	protected DefyndianEnvelope<? extends DefyndianMessage> getMessageFromInbox() throws InterruptedException{
		return getMessageFromInbox(TIMEOUT_SECONDS);
	}
	
	protected DefyndianEnvelope<? extends DefyndianMessage> getMessageFromInbox(long timeoutSeconds) throws InterruptedException{
		return inbox.poll(timeoutSeconds, TimeUnit.SECONDS);
	}
	
	protected final void putMessageInOutbox(DefyndianRoutingType routing, DefyndianMessage message) throws InterruptedException{
		putMessageInOutbox(routing, "", message);
	}
	
	protected final void putMessageInOutbox(DefyndianRoutingType routing, String extra, DefyndianMessage message) throws InterruptedException{
		DefyndianRoutingKey routingKey = new DefyndianRoutingKey(getName(), routing, extra);
		DefyndianEnvelope<DefyndianMessage> envelope = new DefyndianEnvelope<DefyndianMessage>(RoutingInfo.getRoute(config.getRabbitMQDetails().getExchange(), routingKey), message);
		outbox.put(envelope);
	}
	
	public Iterator<DefyndianEnvelope<? extends DefyndianMessage>> getOutboxMessages(){
		return outbox.iterator();
	}
	
	/**
	 * This method is used during initialisation to decide if this node
	 * is on a network with a Station. After sending a SystemPresenceMessage
	 * any SYSTEM response from station with the extra field set to the name
	 * of this node is counted as a successful contact
	 * @return true if a Station was contacted, false otherwise
	 * @throws InterruptedException 
	 * @throws UnknownHostException 
	 */
	private final boolean contactStation() throws InterruptedException{
		DefyndianMessage message = new SystemPresenceMessage(System.currentTimeMillis(), getName(), host);
		putMessageInOutbox(DefyndianRoutingType.SYSTEM, message);
		DefyndianEnvelope<? extends DefyndianMessage> response = getMessageFromInbox(10);
		if( response==null ) {
			return false;
		}
		else{
			DefyndianRoutingKey routingKey = response.getRoute().getRoutingKey(); 
			return  routingKey.getProducer()==STATION_NAME &
					routingKey.getRoutingType()==DefyndianRoutingType.SYSTEM &
					routingKey.getExtraRouting() == getName();
		}
	}
	
	/**
	 * Creates empty or clears the inbox and outbox queues
	 */
	private void initialiseInboxOutbox(){
		if( inbox!=null )
			inbox.clear();
		else
			inbox = new LinkedBlockingQueue<>(MAX_INBOX_SIZE);
		if( outbox!=null )
			outbox.clear();
		else
			outbox = new LinkedBlockingQueue<>(MAX_OUTBOX_SIZE);
	}
	
	/**
	 * Creates a new DefyndianConfig
	 * @return A new DefyndianConfig
	 * @throws IOException 
	 * @throws DefyndianDatabaseException If there was an error accessing the config database
	 */
	private DefyndianConfig initialiseConfig() throws ConfigInitialisationException{
		return DefyndianConfig.getConfig(getName());
	}
	
	/**
	 * Connect to the database specified in the config
	 * @param dataSource 
	 * @return A new database connection
	 * @throws DefyndianDatabaseException If a connection could not be created
	 */
	private java.sql.Connection initialiseDBConnection(DataSource dataSource) throws DefyndianDatabaseException{
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new DefyndianDatabaseException(e.getMessage());
		}
	}
	
	/**
	 * Create a new connection to the RabbitMQ Server and declare the exchange specified by
	 * default values in the config
	 * @return A new MQConnection
	 * @throws DefyndianMQException If no new connection could be created
	 */
	private Connection initialiseMQConnection(RabbitMQDetails details) throws DefyndianMQException {
		ConnectionFactory factory = details.getConnectionFactory();
		try {
			Connection c = factory.newConnection();
			Channel channel = c.createChannel();
			logger.info("Declaring exchange: " + details.getExchange());
			channel.exchangeDeclare(details.getExchange(), "topic", true);
			channel.close();
			return c;
		} catch (IOException | TimeoutException e) {
			logger.error(e);
			throw new DefyndianMQException(e.getMessage());
		}
	}
}
