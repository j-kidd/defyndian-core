package defyndian.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.rabbitmq.client.ConnectionFactory;

import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;

public abstract class MapBackedConfig extends DefyndianConfig {

	private static final String GLOBAL_NAMESPACE = "defyndian";
	
	protected static final String DB_HOST_KEY = "db.host";
	protected static final String DB_USERNAME_KEY = "db.username";
	protected static final String DB_PASSWORD_KEY = "db.password";
	protected static final String DB_DATABASE_KEY = "db.database";
	
	protected static final String MQ_HOST_KEY = "mq.host";
	protected static final String MQ_USERNAME_KEY = "mq.username";
	protected static final String MQ_PASSWORD_KEY = "mq.password";
	protected static final String MQ_EXCHANGE_KEY = "mq.exchange";
	protected static final String MQ_QUEUE_KEY = "mq.queue";
	protected static final String MQ_ROUTING_KEYS = "mq.routingkeys";
	
	private Map<String, Map<String, String>> config;
	private RabbitMQDetails rabbitMQDetails;
	private DataSource datasource;
	
	public MapBackedConfig(String name, Properties init) throws ConfigInitialisationException {
		super(name, init);
		config = new HashMap<>();
		config.putAll(initialiseConfig());
		rabbitMQDetails = initialiseRabbitMQDetails();
		datasource = initialiseDataSource();
	}
	
	/**
	 * Method to initialise the config with at least MQ and DB parameters to allow 
	 * a node basic functionality
	 */
	protected abstract Map<String, Map<String, String>> initialiseConfig() throws ConfigInitialisationException;
	
	@Override
	public final String get(String key) {
		String nodeValue = getFromNamespace(getName(), key);
		if( nodeValue!=null )
			return nodeValue;
		else
			return getFromNamespace(GLOBAL_NAMESPACE, key);
	}

	@Override
	public final String get(String key, String defaultValue) {
		String nodeValue = getFromNamespace(getName(), key);
		if( nodeValue!=null )
			return nodeValue;
		else{
			String globalValue = getFromNamespace(GLOBAL_NAMESPACE, key);
			if( globalValue!=null )
				return globalValue;
			else 
				return defaultValue;
		}
	}
	
	public Collection<String> getRoutingKeys(){
		String keys = get(MQ_ROUTING_KEYS);
		if( keys == null )
			return Collections.EMPTY_LIST;
		else
			return Arrays.asList(keys.split(","));
	}
	
	public RabbitMQDetails getRabbitMQDetails(){
		return rabbitMQDetails;
	}
	
	public DataSource getDataSource(){
		return datasource;
	}

	
	@Override
	public final void put(String key, String value) {
		config.get(getName()).put(key, value);
	}
	
	public final void putAll(Map<String, String> mappings){
		config.get(getName()).putAll(mappings);
	}
	
	protected final void putGlobal(String key, String value){
		config.get(GLOBAL_NAMESPACE).put(key, value);
	}
	
	protected final void putAllGlobal(Map<String, String> mappings) {
		config.get(GLOBAL_NAMESPACE).putAll(mappings);
	}
	
	protected final String getGlobalNamespaceName() {
		return GLOBAL_NAMESPACE;
	}
	
	private final String getFromNamespace(String namespace, String key){
		Map<String, String> namespaceConfig = config.get(namespace);
		if( namespaceConfig==null )
			return null;
		return namespaceConfig.get(key);
	}
	
	private final RabbitMQDetails initialiseRabbitMQDetails() throws ConfigInitialisationException {
		String exchange = get(MQ_EXCHANGE_KEY);
		String queue = get(MQ_QUEUE_KEY);
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(get(MQ_HOST_KEY));
		connectionFactory.setUsername(get(MQ_USERNAME_KEY));
		connectionFactory.setPassword(get(MQ_PASSWORD_KEY));
		return new RabbitMQDetails(exchange, queue, connectionFactory);
	}
	
	private final DataSource initialiseDataSource() throws ConfigInitialisationException {
		MysqlDataSource datasource = new MysqlDataSource();
		String host = get(DB_HOST_KEY);
		String user = get(DB_USERNAME_KEY);
		String password = get(DB_PASSWORD_KEY);
		String database = get(DB_DATABASE_KEY);
		if( user==null | host==null | password==null | database==null ){
			throw new ConfigInitialisationException("Host, User, Password and database must be specified for database connection");
		}
		
		datasource.setServerName(host);
		datasource.setUser(user);
		datasource.setPassword(password);
		datasource.setDatabaseName(database);
		return datasource;
	}
	
	public String toString(){
		return config.toString();
	}

}