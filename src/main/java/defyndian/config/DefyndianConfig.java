package defyndian.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;


public class DefyndianConfig {

	public static final String HOST_KEY = "host";
	public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "password";
	public static final String DATABASE_KEY = "database";
	
	public static final String EXCHANGE_KEY = "mq.exchange";
	public static final String QUEUE_KEY = "mq.queue";
	public static final String ROUTING_KEYS = "routingkeys";
	
	private static final File BASE_CONFIG_FILE = new File("/usr/local/etc/defyndian/defyndian.conf");
	private Map<String, String> config;
	
	private static MysqlDataSource datasource;
	
	public DefyndianConfig(Map<String, String> c) throws SQLException, FileNotFoundException, IOException{
		config = c;
	}
	
	public String get(String key){
		return config.get(key);
	}
	
	public String get(String key, String defaultValue){
		String value = config.get(key);
		if( value==null ){
			return defaultValue;
		}
		else
			return value;
	}
	
	private static void loadLocalProperties() throws FileNotFoundException, IOException{
		Properties localConfig = new Properties();
		localConfig.load(new BufferedReader( new FileReader( BASE_CONFIG_FILE )));
		MysqlDataSource newDatasource = new MysqlDataSource();
		String host = localConfig.getProperty(HOST_KEY);
		String username = localConfig.getProperty(USERNAME_KEY);
		String password = localConfig.getProperty(PASSWORD_KEY);
		String database = localConfig.getProperty(DATABASE_KEY);
		newDatasource.setServerName(host);
		newDatasource.setUser(username);
		newDatasource.setPassword(password);
		newDatasource.setDatabaseName(database);
		datasource = newDatasource;
	}
	
	public MysqlDataSource getDatasource(){
		return datasource;
	}
	
	public static DefyndianConfig loadConfig() throws SQLException, FileNotFoundException, IOException{
		Map<String, String> config = new HashMap<>();
		loadLocalProperties();
		
		config = DatabaseUtil.getDatabaseConfig(datasource);
		
		return new DefyndianConfig(config);
	}
	
	public String toString(){
		if( config==null ){
			return "[CONFIG UNINITIALISED]";
		}
		StringBuilder builder = new StringBuilder();
		ArrayList<String> configKeys = new ArrayList<String>(config.keySet());
		Collections.sort(configKeys);
		for( String key :  configKeys){
			builder.append(key + " : " + get(key));
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}
	
	public static void main(String...args){
		try {
			System.out.println(DefyndianConfig.loadConfig());
		} catch (SQLException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}