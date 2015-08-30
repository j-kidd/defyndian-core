package defyndian.core;

import java.io.IOException;
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

public abstract class DefyndianNode {

	private static final int MAX_INBOX_SIZE = 10;
	private static final int MAX_OUTBOX_SIZE = 5;
	private static final long TIMEOUT_SECONDS = 0;
	private Connection mqConnection;
	private java.sql.Connection dbConnection;
	private DefyndianConfig config;
	
	protected Logger logger;
	private boolean STOP = false;
	private LinkedBlockingQueue<DefyndianMessage> inbox;
	private LinkedBlockingQueue<DefyndianEnvelope> outbox;
	private Publisher publisher;
	private Consumer consumer;
	
	private String name;
	
	public DefyndianNode(String name) throws DefyndianMQException, DefyndianDatabaseException{
		this.name = name;
		logger = Logger.getLogger(name);
		BasicConfigurator.configure();
		config = initialiseConfig();
		mqConnection = initialiseMQConnection();
		dbConnection = initialiseDBConnection();
		initialiseQueues();
		try{
			publisher = new Publisher(outbox, mqConnection.createChannel(), logger);
		} catch( IOException e ){
			throw new DefyndianMQException(e.getMessage());
		}
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
		if( consumer!=null )
			consumer.setStop();
		if( publisher!=null )
			publisher.setStop();
	}
	
	public String getName(){
		return name;
	}
	
	protected void setPublisher() throws DefyndianMQException{
		try {
			publisher = new Publisher(outbox, mqConnection.createChannel(), logger);
		} catch (IOException e) {
			throw new DefyndianMQException("Could not create new channel for publishing");
		}
	}
	
	protected void setConsumer(String queue) throws DefyndianMQException{
		try {
			consumer = new Consumer(inbox, mqConnection.createChannel(), queue, logger);
		} catch (IOException e) {
			throw new DefyndianMQException("Could not create new channel for publishing");
		}
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
	
	public void putMessageInOutbox(DefyndianEnvelope envelope){
		outbox.add(envelope);
	}
	
	public Iterator<DefyndianEnvelope> getOutboxMessages(){
		return outbox.iterator();
	}
	
	private void initialiseQueues(){
		if( inbox!=null )
			inbox.clear();
		if( outbox!=null )
			outbox.clear();
		inbox = new LinkedBlockingQueue<>(MAX_INBOX_SIZE);
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
