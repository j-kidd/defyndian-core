package defyndian.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.rabbitmq.client.ConnectionFactory;

import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.messaging.DefyndianRoutingKey;
import defyndian.messaging.InvalidRoutingKeyException;

public abstract class MapBackedConfig extends DefyndianConfig {

	private static final String GLOBAL_NAMESPACE = "defyndian";
	
	protected static final String DATASTORE_KEY = "datastore.type";
	
	protected static final String MQ_HOST_KEY = "mq.host";
	protected static final String MQ_USERNAME_KEY = "mq.username";
	protected static final String MQ_PASSWORD_KEY = "mq.password";
	protected static final String MQ_EXCHANGE_KEY = "mq.exchange";
	protected static final String MQ_QUEUE_KEY = "mq.queue";
	protected static final String MQ_VIRTUAL_HOST_KEY = "mq.virtualHost";
	protected static final String DEFAULT_VIRTUAL_HOST = "defyndian";
	protected static final String MQ_ROUTING_KEYS = "mq.routingkeys";
	
	private Map<String, String> config;
	private RabbitMQDetails rabbitMQDetails;
	private Collection<DefyndianRoutingKey> routingKeys;

	public MapBackedConfig(String name, Properties init) throws ConfigInitialisationException {
		super(name);
		config = init.stringPropertyNames().stream()
					.collect(Collectors.toMap(Function.identity(), init::getProperty));
		config.putAll(initialiseConfig());
		rabbitMQDetails = initialiseRabbitMQDetails();
		routingKeys = convertRoutingKeys(get(MQ_ROUTING_KEYS));

	}
	
	/**
	 * Method to initialise the config with extra parameters
	 */
	protected abstract Map<String, String> initialiseConfig() throws ConfigInitialisationException;
	
	@Override
	public final String get(String key) {
		return config.get(key);
	}

	@Override
	public Collection<DefyndianRoutingKey> getRoutingKeys() {
		return routingKeys;
	}

	@Override
	public RabbitMQDetails getRabbitMQDetails(){
		return rabbitMQDetails;
	}

	@Override
	public final void put(String key, String value) {
		config.put(key, value);
	}
	
	public final void putAll(Map<String, String> mappings){
		config.putAll(mappings);
	}

	public final String get(String key, String defaultValue){
		final String value = config.get(key);
		if( value==null )
			return defaultValue;
		else
			return value;
	}
	
	private final RabbitMQDetails initialiseRabbitMQDetails() throws ConfigInitialisationException {
		String exchange = get(MQ_EXCHANGE_KEY);
		String queue = get(MQ_QUEUE_KEY);
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(get(MQ_HOST_KEY));
		connectionFactory.setUsername(get(MQ_USERNAME_KEY));
		connectionFactory.setPassword(get(MQ_PASSWORD_KEY));
		connectionFactory.setVirtualHost(get(MQ_VIRTUAL_HOST_KEY, DEFAULT_VIRTUAL_HOST));
		return new RabbitMQDetails(exchange, queue, connectionFactory);
	}
	
	private Collection<DefyndianRoutingKey> convertRoutingKeys(String keys) throws ConfigInitialisationException{
		LinkedList<DefyndianRoutingKey> convertedKeys = new LinkedList<>();
		
		if( keys == null )
			return convertedKeys;

		for( String s : keys.split(",") ){
			try{
				convertedKeys.add(new DefyndianRoutingKey(s));
			} catch( InvalidRoutingKeyException e ){
				throw new ConfigInitialisationException(e);
			}
		}
		return convertedKeys;
	}
	
	public String toString(){
		return config.toString();
	}

}