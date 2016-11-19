package defyndian.config;

import com.rabbitmq.client.ConnectionFactory;
import defyndian.exception.ConfigInitialisationException;
import defyndian.messaging.routing.DefyndianRoutingKey;
import defyndian.messaging.routing.InvalidRoutingKeyException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import static defyndian.config.ConfigKeys.*;

public abstract class MapBackedConfig extends DefyndianConfig {

	protected static final String DEFAULT_VIRTUAL_HOST = "defyndian";

	
	private Map<String, String> config;
	private RabbitMQDetails rabbitMQDetails;
	private Collection<DefyndianRoutingKey> routingKeys;

	public MapBackedConfig(String name, Properties init) throws ConfigInitialisationException {
		super(name);
		config = init.stringPropertyNames().stream()
					.collect(Collectors.toMap(Function.identity(), init::getProperty));
		config.putAll(initialiseConfig());
		rabbitMQDetails = initialiseRabbitMQDetails();
		routingKeys = convertRoutingKeys(get(mq_routingkeys.toString()));
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
		String exchange = get(mq_exchange.toString());
		String queue = get(mq_queue.toString());
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(get(mq_host.toString()));
		connectionFactory.setUsername(get(mq_username.toString()));
		connectionFactory.setPassword(get(mq_password.toString()));
		connectionFactory.setVirtualHost(get(mq_virtualhost.toString(), DEFAULT_VIRTUAL_HOST));
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