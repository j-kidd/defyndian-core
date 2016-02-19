package defyndian.core;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import defyndian.config.DefyndianConfig;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.DefyndianEnvelope;
import defyndian.messaging.DefyndianMessage;
import defyndian.messaging.BasicDefyndianMessage;
import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.DefyndianRoutingType;
import defyndian.messaging.RoutingInfo;

/**
 * This is the base class for all components of the system, it contains the Inbox/Outbox 
 * structures and the ability to start a publisher or consumer. It manages the MQ and DB
 * connections, logger and config.
 * @author james
 *
 */
public abstract class DefyndianNode implements AutoCloseable{

	private static final String DESCRIPTION = "This is a default Defyndian Node description";
	
	private static final int MAX_INBOX_SIZE = 10;
	private static final int MAX_OUTBOX_SIZE = 5;
	private static final long TIMEOUT_SECONDS = 5;
	private Connection mqConnection;
	private java.sql.Connection dbConnection;
	protected DefyndianConfig config;
	
	protected Logger logger;
	private boolean STOP = false;
	private LinkedBlockingQueue<DefyndianMessage> inbox;
	private LinkedBlockingQueue<DefyndianEnvelope> outbox;
	protected Publisher publisher;
	protected Consumer consumer;
	
	private String name;
	
	/**
	 * The constructor initialises all managed resources and declares the
	 * queues/exchanges from the config
	 * @param name
	 * @throws DefyndianMQException If the MQConnection could not be initialised
	 * @throws DefyndianDatabaseException If the DBConnection could not be initialised
	 */
	public DefyndianNode(String name) throws DefyndianMQException, DefyndianDatabaseException{
		this.name = name;
		logger = LogManager.getLogger(name);
		config = initialiseConfig();
		mqConnection = initialiseMQConnection();
		dbConnection = initialiseDBConnection();
		initialiseQueues();
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
	 * @return
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
	
	/**
	 * Initialises the published for this node, the publisher will read messages from the outbox
	 * and deliver them to the system
	 * @throws DefyndianMQException If no new channel could be created within the MQConnection
	 */
	protected void setPublisher() throws DefyndianMQException{
		try {
			publisher = new Publisher(outbox, mqConnection.createChannel(), logger);
		} catch (IOException e) {
			throw new DefyndianMQException("Could not create new channel for publishing");
		}
	}
	
	/**
	 * Initialises a consumer with the exchange/queue values from the config,
	 * the consumer will read messages from the MQ Server and deliver them to the inbox
	 * @throws DefyndianMQException If there is no exchange specified, or a channel could not be created
	 */
	protected void setConsumer() throws DefyndianMQException{
		String exchange = getConfigValue(DefyndianConfig.EXCHANGE_KEY);
		String queue = getConfigValue(DefyndianConfig.QUEUE_KEY);
		logger.debug("Got queue : " + queue + " with key: " + DefyndianConfig.QUEUE_KEY);
		if( exchange==null ){
			throw new DefyndianMQException("No exchange specified in config");
		}
		if( queue == null ){
			queue = getName();
		}
		setConsumer(exchange, queue);
	}
	
	/**
	 * More specific method to initialised a consumer, though with the same outcome
	 * @param exchange The exchange to listen on
	 * @param queue The Queue to listen on
	 * @throws DefyndianMQException If no channel could be created
	 */
	protected void setConsumer(String exchange, String queue) throws DefyndianMQException{
		try {
			consumer = new Consumer(inbox, mqConnection.createChannel(), exchange, queue, getConfigValue(DefyndianConfig.ROUTING_KEYS), logger);
			consumer.start(InetAddress.getLocalHost().getHostName() + "-" + getName());
		} catch (IOException e) {
			throw new DefyndianMQException("Could not create new channel for publishing");
		}
	}
	
	/**
	 * Checks if the config contains a value for the given key, config accesses within a node are first
	 * looked for as node specific ie. NODE_NAME.key and then for the default value ie. key
	 * @param key The key to get
	 * @return True if there is a value for this key (either node specific or default)
	 */
	public boolean hasConfigValue(String key){
		String value = config.get(getName() + "." + key);
		if( value!=null ){
			return true;
		}
		else
			return config.get(key)!=null;
	}
	
	/**
	 * Get the value from config, either node-specific of default if none exists
	 * @param key Key to get
	 * @return The value from the config, or null if no such key exists
	 */
	public String getConfigValue(String key){
		String value = config.get(getName() + "." + key);
		if( value==null ){
			value = config.get(key);
		}
		return value;
	}
	
	/**
	 * As getConfigValue but defaultValue will be returned if no key exists
	 * @param key The key to get
	 * @param defaultValue The return value if the key is not found
	 * @return Either the value of the given key or defaultValue
	 */
	public String getConfigValue(String key, String defaultValue){
		String value = config.get(getName() + "." + key);
		if( value==null ){
			value = config.get(key);
		}
		if( value==null )
			return defaultValue;
		else
			return value;
	}
	
	public String getName(){
		return name;
	}
	
	public Connection getMQConnection(){
		return mqConnection;
	}
	
	public java.sql.Connection getDBConnection(){
		return dbConnection;
	}
	
	public DefyndianMessage getMessageFromInbox() throws InterruptedException{
		return inbox.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}
	
	protected void putMessageInOutbox(DefyndianEnvelope envelope) throws InterruptedException{
		outbox.put(envelope);
	}
	
	protected void putMessageInOutbox(String exchange, DefyndianRoutingKey routingKey, DefyndianMessage message) throws InterruptedException{
		putMessageInOutbox(new DefyndianEnvelope(RoutingInfo.getRoute(exchange, routingKey), message));
	}
	
	protected void putMessageInOutbox(DefyndianMessage message) throws InterruptedException{
		putMessageInOutbox(new DefyndianEnvelope(RoutingInfo.getRoute(	getConfigValue(DefyndianConfig.EXCHANGE_KEY),
																		makeRoutingKey()
																	)
												, message)
							);
	}
	
	public Iterator<DefyndianEnvelope> getOutboxMessages(){
		return outbox.iterator();
	}
	
	protected DefyndianRoutingKey makeRoutingKey(DefyndianRoutingType type, String extra){
		return new DefyndianRoutingKey(getName(), type, extra);
	}
	
	protected DefyndianRoutingKey makeRoutingKey(String extra){
		return makeRoutingKey(DefyndianRoutingType.DEFAULT, extra);
	}
	
	protected DefyndianRoutingKey makeRoutingKey(){
		return makeRoutingKey("");
	}
	
	/**
	 * Creates empty or clears the inbox and outbox queues
	 */
	private void initialiseQueues(){
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
	 * @throws DefyndianDatabaseException If there was an error accessing the config database
	 */
	private DefyndianConfig initialiseConfig() throws DefyndianDatabaseException{
		try {
			return DefyndianConfig.loadConfig();
		} catch (SQLException | IOException e) {
			throw new DefyndianDatabaseException(e.getMessage());
		}
	}
	
	/**
	 * Connect to the database specified in the config
	 * @return A new database connection
	 * @throws DefyndianDatabaseException If a connection could not be created
	 */
	private java.sql.Connection initialiseDBConnection() throws DefyndianDatabaseException{
		try {
			return config.getDatasource().getConnection();
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
	private Connection initialiseMQConnection() throws DefyndianMQException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setAutomaticRecoveryEnabled(true);
		factory.setUsername(config.get("mq.username"));
		factory.setVirtualHost(config.get("mq.virtualhost"));
		factory.setPassword(config.get("mq.password"));
		factory.setHost(config.get("mq.hostname"));
		try {
			Connection c = factory.newConnection();
			Channel channel = c.createChannel();
			channel.exchangeDeclare(config.get("mq.exchange"), "topic", true);
			channel.close();
			return c;
		} catch (IOException | TimeoutException e) {
			logger.error(e);
			throw new DefyndianMQException(e.getMessage());
		}
	}
}
