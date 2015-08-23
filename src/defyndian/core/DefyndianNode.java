package defyndian.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import messaging.DefyndianMessage;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import defyndian.config.DefyndianConfig;
import exception.DefyndianDatabaseException;
import exception.DefyndianMQException;

public abstract class DefyndianNode {

	private static final int MAX_INBOX_SIZE = 10;
	private static final int MAX_OUTBOX_SIZE = 5;
	private Connection mqConnection;
	private java.sql.Connection dbConnection;
	private DefyndianConfig config;
	
	protected Logger logger;
	private LinkedBlockingQueue<DefyndianMessage> inbox;
	private LinkedBlockingQueue<DefyndianMessage> outbox;
	private Publisher publisher;
	
	private String name;
	
	public DefyndianNode(String name) throws DefyndianMQException, DefyndianDatabaseException{
		this.name = name;
		logger = Logger.getLogger(name);
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
	
	public abstract void start() throws Exception;
	
	public String getName(){
		return name;
	}
	
	public Connection getMQConnection(){
		return mqConnection;
	}
	
	public java.sql.Connection getDBConnection(){
		return dbConnection;
	}
	
	public DefyndianMessage getMessageFromInbox(){
		return inbox.poll();
	}
	
	public void putMessageInOutbox(DefyndianMessage message){
		outbox.add(message);
	}
	
	public Iterator<DefyndianMessage> getOutboxMessages(){
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
		factory.setUsername(config.get("mq.password"));
		factory.setUsername(config.get("mq.virtualHost"));
		factory.setUsername(config.get("mq.hostName"));
		factory.setUsername(config.get("mq.portNumber"));
		try {
			return factory.newConnection();
		} catch (IOException | TimeoutException e) {
			throw new DefyndianMQException(e.getMessage());
		}
	}
}
