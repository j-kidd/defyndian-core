package defyndian.core;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import defyndian.config.DefyndianConfig;
import defyndian.config.RabbitMQDetails;
import defyndian.datastore.DatastoreBuilder;
import defyndian.datastore.DefyndianDatastore;
import defyndian.datastore.FileSerializationDataStore;
import defyndian.datastore.exception.DatastoreCreationException;
import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * This is the base class for all components of the system, it contains the Inbox/Outbox 
 * structures and the ability to start a publisher or consumer. It manages the MQ and DB
 * connections, logger and config.
 * @author james
 *
 */
public abstract class DefyndianNode implements AutoCloseable{

	private static final String STATION_NAME = "Station";
	private static final long TIMEOUT_SECONDS = 5;
    private static final Logger logger = LoggerFactory.getLogger(DefyndianNode.class);

	protected DefyndianConfig config;

	private volatile boolean STOP = false;
	protected Publisher publisher;
	protected Consumer consumer;
    protected DefyndianDatastore datastore;
	private final String name;

	/**
	 * The constructor initialises all managed resources and declares the
	 * queues/exchanges from the config
	 * @param name
	 * @throws DefyndianMQException If the MQConnection could not be initialised
	 * @throws DefyndianDatabaseException If the DBConnection could not be initialised
	 * @throws ConfigInitialisationException 
	 */
	public DefyndianNode(String name, Connection connection) throws DefyndianMQException, ConfigInitialisationException, DatastoreCreationException, IOException {
		this(name, connection, initialiseConfig(name));
	}

    /**
     * Fully specified constructor, to be used by container ( when one is added ? ) and
     * for use in testing
     * @param name
     * @param config
     * @throws DefyndianMQException
     */
	public DefyndianNode(String name, Connection connection, DefyndianConfig config) throws DefyndianMQException, DatastoreCreationException, IOException {
		this(   name,
                config,
                initialisePublisher(connection),
                initialiseConsumer(name, config.getRoutingKeys(), connection, config.getRabbitMQDetails()),
                new FileSerializationDataStore<>("DefaultFileStore")
		);
	}

    public DefyndianNode(String name,
                         DefyndianConfig config,
                         Publisher publisher,
                         Consumer consumer,
                         DefyndianDatastore datastore){
        this.name = name;
        this.config = config;
        this.publisher = publisher;
        this.consumer = consumer;
        this.datastore = datastore;
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
	}

    /**
     * Take a message from the inbox, waiting if necessary for one to arrive
     * @return A DefyndianEnvelope sent to this node
     * @throws InterruptedException
     */
    protected DefyndianEnvelope<? extends DefyndianMessage> consume() throws InterruptedException{
        return consumer.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Publishes a message in an envelope to the specified route
     * @param envelope
     * @throws InterruptedException
     */
    protected void publish(DefyndianEnvelope<? extends DefyndianMessage> envelope) throws InterruptedException{
        publisher.publish(envelope);
    }

    protected void publish(String exchange, DefyndianRoutingKey routingKey, DefyndianMessage message) throws InterruptedException{
        publish(new DefyndianEnvelope<DefyndianMessage>(RoutingInfo.getRoute(exchange, routingKey), message));
    }

    protected void publish(DefyndianMessage message) throws InterruptedException{
        publish(new DefyndianEnvelope<DefyndianMessage>(RoutingInfo.getRoute(
                config.getRabbitMQDetails().getExchange(),
                DefyndianRoutingKey.getDefaultKey(getName())
                )
                        , message)
        );
    }


	/**
	 * Returns a newly initialised publisher,it will read messages from the outbox
	 * and deliver them to the system
	 * @throws DefyndianMQException If no new channel could be created within the MQConnection
	 */
	protected static Publisher initialisePublisher(Connection connection) throws DefyndianMQException{
        try {
            return new Publisher(connection.createChannel());
        } catch (IOException e) {
            throw new DefyndianMQException("Couldn't create new channel for Publisher");
        }
    }
	
	/**
	 * Initialises a consumer with the exchange/queue values from the config,
	 * the consumer will read messages from the MQ Server and deliver them to the inbox
	 * @throws DefyndianMQException If there is no exchange specified, or a channel could not be created
	 */
	protected static Consumer initialiseConsumer(String name, Collection<DefyndianRoutingKey> routingKeys, Connection connection, RabbitMQDetails details) throws DefyndianMQException{
		final Consumer consumer;
        try {
			consumer = new Consumer(connection.createChannel(), details, routingKeys);
            consumer.bindConsumer(String.format("*."+ DefyndianRoutingType.DIRECT+".%s", name));
			consumer.start(InetAddress.getLocalHost().getHostName() + "-" + name);
            return consumer;
		} catch (IOException e) {
			throw new DefyndianMQException("Could not create new channel for publishing");
		}
	}

	public String getName(){
		return name;
	}
	
	public static final String getStationName(){
		return STATION_NAME;
	}
	
	/**
	 * Creates a new DefyndianConfig
	 * @return A new DefyndianConfig
	 * @throws IOException 
	 * @throws DefyndianDatabaseException If there was an error accessing the config database
	 */
	private static final DefyndianConfig initialiseConfig(String name) throws ConfigInitialisationException{
		return DefyndianConfig.getConfig(name);
	}
	
	/**
	 * Connect to the database specified in the config
	 * @param dataSource 
	 * @return A new database connection
	 * @throws DefyndianDatabaseException If a connection could not be created
	 */
	private static final java.sql.Connection initialiseDBConnection(DataSource dataSource) throws DefyndianDatabaseException{
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
	private static final Connection initialiseMQConnection(RabbitMQDetails details) throws DefyndianMQException {
		ConnectionFactory factory = details.getConnectionFactory();
		try {
			Connection c = factory.newConnection();
			Channel channel = c.createChannel();
			logger.info("Declaring exchange: " + details.getExchange());
			channel.exchangeDeclare(details.getExchange(), "topic", true);
			channel.close();
			return c;
		} catch (IOException | TimeoutException e) {
			throw new DefyndianMQException("Couldn't initialise MQ Connection", e);
		}
	}
}
