package defyndian.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import javax.sql.DataSource;

import defyndian.exception.ConfigInitialisationException;
import defyndian.messaging.DefyndianRoutingKey;

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
	private static final String CONFIG_PROPERTIES = "defyndian.properties";
	private static final String CONFIG_TYPE_KEY = "config.type";
	private static Properties conf;
	
	private final String name;
	
	protected DefyndianConfig(String name, Properties initialisation){
		this.name = name;
		conf = initialisation;
	}
	
	public static DefyndianConfig getConfig(String name, File propertiesFile) throws ConfigInitialisationException{
		conf = new Properties();
		try{
			conf.load(new BufferedReader(new FileReader(propertiesFile)));
		} catch( IOException e){
			throw new ConfigInitialisationException(e);
		}
		return ConfigType.valueOf(conf.getProperty(CONFIG_TYPE_KEY, DEFAULT_CONFIG_TYPE.toString())).getConfig(name, conf);
	}
	
	public static DefyndianConfig getConfig(String name) throws ConfigInitialisationException{
		File configProperties;
		try {
			URL properties = DefyndianConfig.class.getClassLoader().getResource(CONFIG_PROPERTIES);
			if( properties == null ){
				throw new ConfigInitialisationException(new FileNotFoundException("No " + CONFIG_PROPERTIES + " file on classpath"));
			}
			configProperties = new File(properties.toURI());
			if( ! configProperties.canRead() )
				throw new ConfigInitialisationException("Initialisation Properties file " + CONFIG_PROPERTIES + " isn't readable/cannot be found");
			return getConfig(name, configProperties);
		} catch (URISyntaxException e) {
			throw new ConfigInitialisationException(e);
		}
		
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
	
	public abstract Collection<DefyndianRoutingKey> getRoutingKeys();
	
	protected final String getName(){
		return name;
	}
	
	public String getFromLocal(String key){
		return conf.getProperty(key);
	}
	
}
	
	