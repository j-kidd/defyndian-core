package defyndian.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Properties;

import javax.sql.DataSource;

import defyndian.exception.ConfigInitialisationException;

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

	private static final String DEFAULT_CONFIG_TYPE = ConfigType.BASIC.toString();
	private static final String CONFIG_PROPERTIES = "defyndian.conf";
	private static final String CONFIG_TYPE_KEY = "config.type";
	private static Properties conf;
	
	private final String name;
	
	protected DefyndianConfig(String name){
		this.name = name;
	}
	
	public static DefyndianConfig getConfig(String name) throws ConfigInitialisationException{
		conf = new Properties();
		try{
			conf.load(new BufferedReader(new InputStreamReader(DefyndianConfig.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES))));
		} catch( IOException e){
			throw new ConfigInitialisationException(e);
		}
		return ConfigType.valueOf(conf.getProperty(CONFIG_TYPE_KEY, DEFAULT_CONFIG_TYPE)).getConfig(name);
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
	 * Returns the datasource to use for the main database used by nodes
	 * @return A datasource from node ofr global namespace to connect
	 * to the main database on
	 */
	public abstract DataSource getDataSource();
	
	/**
	 * Method to persist inserted values to backing store
	 */
	public abstract void save();
	
	public abstract Collection<String> getRoutingKeys();
	
	protected final String getName(){
		return name;
	}
	
	public String getFromLocal(String key){
		return conf.getProperty(key);
	}
	
}
	
	