package defyndian.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import defyndian.exception.ConfigInitialisationException;
import defyndian.messaging.routing.DefyndianRoutingKey;

/**
 * Super class for all config classes, subclasses should implement
 * the backing behaviour of this class to provide an actual config.
 * Keys are stored in a namespace, nodes look up
 * keys in their own namespace and then in the global namespace; all 
 * values added to the config are added under the nodes namespace.
 * All values are non-null.
 * @author james
 *
 */
public abstract class DefyndianConfig {

	private static final ConfigType DEFAULT_CONFIG_TYPE = ConfigType.BASIC;
	private static final String CONFIG_TYPE_KEY = "config.type";
	private final String name;

	protected DefyndianConfig(String name){
		this.name = name;
	}

	public static DefyndianConfig getConfig(String name) throws ConfigInitialisationException{
		final String configFileName = String.format("%s.properties", name);
		final File configProperties = new File(configFileName);
		if( ! configProperties.canRead() )
			throw new ConfigInitialisationException("Initialisation Properties file " + configFileName + " isn't readable/cannot be found");
		final Properties conf = new Properties();
		try {
			conf.load(new FileReader(configProperties));
		} catch (IOException e) {
			throw new ConfigInitialisationException("Error while reading properties", e);
		}
		final ConfigType configType = ConfigType.valueOf(conf.getProperty(CONFIG_TYPE_KEY, DEFAULT_CONFIG_TYPE.toString()));
		return configType.getConfig(name, conf);
	}
	/**
	 * Retrieve the value for this key
	 * @param key String value of the key to get
	 * @return The value for this key, or null if no mapping for this key exists
	 */
	public abstract String get(String key);
	
	/**
	 * Retrieve the value for this key, obeying namespace
	 * rules but returning the given default value if no
	 * mapping exists
	 * @param key Key to retrieve value for
	 * @param defaultValue Value to return if no key in config
	 * @return The value for the given key, or defaultValue if no such value exists
	 */
	public abstract String get(String key, String defaultValue);
	
	/**
	 * Add this mapping to the namespace of this node, it will only be available
	 * to this node
	 * @param key 
	 * @param value 
	 */
	public abstract void put(String key, String value);

	/**
	 * This method is used by Nodes to determine the details of the
	 * AMQP broker
	 * @return A RabbitMQDetails object containing the node or global 
	 * specs of the broker
	 */
	public abstract RabbitMQDetails getRabbitMQDetails();

	/**
	 * Method to persist inserted values to backing store
	 */
	public abstract void save();
	
	public abstract Collection<DefyndianRoutingKey> getRoutingKeys();
	
	protected final String getName(){
		return name;
	}
}
	
	