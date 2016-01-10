package defyndian.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DefyndianConfig {

	public static final String HOST_KEY = "host";
	public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "password";
	public static final String DATABASE_KEY = "database";
	
	public static final String EXCHANGE_KEY = "mq.exchange";
	public static final String QUEUE_KEY = "mq.queue";
	public static final String ROUTING_KEYS = "routingkeys";
	
	public static final String SECTION_KEY = "section";
	public static final String KEY_KEY = "configKey";
	public static final String VALUE_KEY = "configValue";
	
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
	
	private static DataSource loadDatasourceFromLocalProperties() throws FileNotFoundException, IOException{
		Properties localConfig = new Properties();
		localConfig.load(new BufferedReader( new FileReader( BASE_CONFIG_FILE )));
		MysqlDataSource newDatasource = new MysqlDataSource();
		newDatasource.setServerName(localConfig.getProperty(HOST_KEY));
		newDatasource.setUser(localConfig.getProperty(USERNAME_KEY));
		newDatasource.setPassword(localConfig.getProperty(PASSWORD_KEY));
		newDatasource.setDatabaseName(localConfig.getProperty(DATABASE_KEY));
		return newDatasource;
	}
	
	public MysqlDataSource getDatasource(){
		return datasource;
	}
	
	public static DefyndianConfig loadConfig() throws SQLException, FileNotFoundException, IOException{
		Map<String, String> config = new HashMap<>();
		config = getDatabaseConfig(loadDatasourceFromLocalProperties());
		return new DefyndianConfig(config);
	}
	
	private static final Map<String, String> getDatabaseConfig(DataSource datasource) throws SQLException{
		HashMap<String, String> conf = new HashMap<>();
		Connection connection = datasource.getConnection();
		
		PreparedStatement statement = connection.prepareCall("call getConfig()");
		ResultSet results = statement.executeQuery();
		while( results.next() ){
			conf.put(results.getString(KEY_KEY), results.getString(VALUE_KEY));
		}
		return conf;
	}
	
	public String toString(){
		if( config==null ){
			return "[CONFIG UNINITIALISED]";
		}
		StringBuilder builder = new StringBuilder();
		ArrayList<String> configKeys = new ArrayList<>(config.keySet());
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
