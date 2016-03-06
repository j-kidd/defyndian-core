package defyndian.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.rabbitmq.client.ConnectionFactory;

import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;

public class MysqlConfig extends MapBackedConfig{

	private static final String NAMESPACE_KEY = "configNamespace";
	private static final String KEY_KEY = "configKey";
	private static final String VALUE_KEY = "configValue";
	
	private static final String CONFIG_DB_HOST = "config.db.host";
	private static final String CONFIG_DB_USERNAME = "config.db.username";
	private static final String CONFIG_DB_PASSWORD = "config.db.password";
	
	private static final String GET_CONFIG_QUERY = "SELECT * from defyndian.config where namespace = '?' OR namespace = '?'";

	public MysqlConfig(String name, Properties init) throws Exception{
		super(name, init);
		initialiseConfig();
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}
	
	protected Map<String, Map<String, String>> initialiseConfig() throws ConfigInitialisationException {
		HashMap<String, Map<String, String>> conf = new HashMap<>();
		
		try( Connection connection = datasourceFromProperties().getConnection() ){
		
		PreparedStatement statement = connection.prepareCall(GET_CONFIG_QUERY);
		ResultSet results = statement.executeQuery();
		while( results.next() ){
			String namespace = results.getString(NAMESPACE_KEY);
			String key = results.getString(KEY_KEY);
			String value = results.getString(VALUE_KEY);
			Map<String, String> namespaceConfig = conf.getOrDefault(namespace, new HashMap<String, String>());
			namespaceConfig.put(key, value);
		}
		} catch (SQLException s){
			throw new ConfigInitialisationException("SQLException while getting config from db, " + s.getMessage());
		}
		return conf;
	}
	
	private DataSource datasourceFromProperties(){
		MysqlDataSource datasource = new MysqlDataSource();
		datasource.setURL(getFromLocal(CONFIG_DB_HOST));
		datasource.setUser(getFromLocal(CONFIG_DB_USERNAME));
		datasource.setPassword(getFromLocal(CONFIG_DB_PASSWORD));
		return datasource;
	}

	


	
	
}
