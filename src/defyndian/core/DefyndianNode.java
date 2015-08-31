package defyndian.core;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import defyndian.config.DefyndianConfig;
import exception.DefyndianDatabaseException;
import exception.DefyndianMQException;
import messaging.DefyndianEnvelope;
import messaging.DefyndianMessage;
import messaging.DefyndianRoutingKey;
import messaging.DefyndianRoutingType;
import messaging.RoutingInfo;

public abstract class DefyndianNode {

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
	
	public DefyndianNode(String name) throws DefyndianMQException, DefyndianDatabaseException{
		this.name = name;
		logger = Logger.getLogger(name);
		BasicConfigurator.configure();
		config = initialiseConfig();
		mqConnection = initialiseMQConnection();
		dbConnection = initialiseDBConnection();
		initialiseQueues();
	}
	
	protected boolean topShouldExit(){
		if( STOP==true )
			return true;
		else
			return shouldExit();
	}
	
	protected void setup(){
		logger.info("No setup specified, using default");
	}
	
	protected boolean shouldExit(){
		return false;
	}
	
	public abstract void start() throws Exception;
	
	public void stop(){
		if( publisher!=null )
			publisher.setStop();
	}
	
	
	protected void setPublisher() throws DefyndianMQException{
		try {
			publisher = new Publisher(outbox, mqConnection.createChannel(), logger);
		} catch (IOException e) {
			throw new DefyndianMQException("Could not create new channel for publishing");
		}
	}
	
	protected void setConsumer() throws DefyndianMQException{
		setConsumer(getConfigValue(DefyndianConfig.EXCHANGE_KEY), getConfigValue(DefyndianConfig.QUEUE_KEY));
	}
	
	protected void setConsumer(String exchange, String queue) throws DefyndianMQException{
		try {
			consumer = new Consumer(inbox, mqConnection.createChannel(), exchange, queue, getConfigValue(DefyndianConfig.ROUTING_KEYS), logger);
			consumer.start(InetAddress.getLocalHost().getHostName() + "-" + getName());
		} catch (IOException e) {
			throw new DefyndianMQException("Could not create new channel for publishing");
		}
	}
	
	public String getConfigValue(String key){
		return config.get(getName() + "." + key);
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
		return new DefyndianRoutingKey(getName(), DefyndianRoutingType.DEFAULT, extra);
	}
	
	protected DefyndianRoutingKey makeRoutingKey(){
		return new DefyndianRoutingKey(getName(), DefyndianRoutingType.DEFAULT, "");
	}
	
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
	
	private DefyndianConfig initialiseConfig() throws DefyndianDatabaseException{
		try {
			return DefyndianConfig.loadConfig();
		} catch (SQLException | IOException e) {
			throw new DefyndianDatabaseException(e.getMessage());
		}
	}
	
	private java.sql.Connection initialiseDBConnection() throws DefyndianDatabaseException{
		try {
			return config.getDatasource().getConnection();
		} catch (SQLException e) {
			throw new DefyndianDatabaseException(e.getMessage());
		}
	}
	
	private Connection initialiseMQConnection() throws DefyndianMQException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setAutomaticRecoveryEnabled(true);
		factory.setUsername(config.get("mq.username"));
		factory.setPassword(config.get("mq.password"));
		factory.setVirtualHost(config.get("mq.virtualhost"));
		factory.setHost(config.get("mq.hostname"));
		try {
			return factory.newConnection();
		} catch (IOException | TimeoutException e) {
			logger.error(e);
			throw new DefyndianMQException(e.getMessage());
		}
	}
}
