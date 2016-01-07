package defyndian.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DatabaseUtil {

	public static final String SECTION_KEY = "section";
	public static final String KEY_KEY = "configKey";
	public static final String VALUE_KEY = "configValue";

	public static final Map<String, String> getDatabaseConfig(MysqlDataSource datasource) throws SQLException{
		HashMap<String, String> conf = new HashMap<>();
		Connection connection = datasource.getConnection();
		
		PreparedStatement statement = connection.prepareCall("call getConfig()");
		ResultSet results = statement.executeQuery();
		while( results.next() ){
			conf.put(results.getString(KEY_KEY), results.getString(VALUE_KEY));
		}
		return conf;
	}
	
	
}
