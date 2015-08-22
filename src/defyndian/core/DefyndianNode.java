package defyndian.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import defyndian.config.DefyndianConfig;

public abstract class DefyndianNode {

	private Connection mqConnection;
	private java.sql.Connection dbConnection;
	private DefyndianConfig config;
	
	protected Logger logger;
	
	private String name;
	
	public DefyndianNode(String name) throws FileNotFoundException, SQLException, IOException, TimeoutException{
		this.name = name;
		logger = Logger.getLogger(name);
		config = initialiseConfig();
		mqConnection = initialiseMQConnection();
		dbConnection = initialiseDBConnection();
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
	
	private DefyndianConfig initialiseConfig() throws FileNotFoundException, SQLException, IOException{
		return DefyndianConfig.loadConfig();
	}
	
	private java.sql.Connection initialiseDBConnection() throws FileNotFoundException, SQLException, IOException{
		return config.getDatasource().getConnection();
	}
	
	private Connection initialiseMQConnection() throws FileNotFoundException, SQLException, IOException, TimeoutException{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setAutomaticRecoveryEnabled(true);
		factory.setUsername(config.get("mq.username"));
		factory.setUsername(config.get("mq.password"));
		factory.setUsername(config.get("mq.virtualHost"));
		factory.setUsername(config.get("mq.hostName"));
		factory.setUsername(config.get("mq.portNumber"));
		return factory.newConnection();
	}
}
