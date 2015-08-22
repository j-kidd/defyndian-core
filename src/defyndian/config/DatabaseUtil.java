package defyndian.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DatabaseUtil {

	public static final String SECTION_KEY = "section";
	public static final String KEY_KEY = "configKey";
	public static final String VALUE_KEY = "configValue";

	public static final LinkedList<HashMap<String, String>> getDatabaseConfig(MysqlDataSource datasource) throws SQLException{
		LinkedList<HashMap<String, String>> rows = new LinkedList<>();
		Connection connection = datasource.getConnection();
		
		PreparedStatement statement = connection.prepareCall("call getConfig()");
		ResultSet results = statement.executeQuery();
		while( results.next() ){
			HashMap<String, String> row = new HashMap<String, String>();
			row.put(KEY_KEY, results.getString(KEY_KEY));
			row.put(VALUE_KEY, results.getString(VALUE_KEY));
			rows.add(row);
		}
		return rows;
		
	}
	
	
}
