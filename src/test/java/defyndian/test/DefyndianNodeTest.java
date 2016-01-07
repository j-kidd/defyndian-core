package defyndian.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import defyndian.config.DatabaseUtil;
import defyndian.config.DefyndianConfig;

public class DefyndianNodeTest {

	private static final Map<String,String> TEST_CONFIG = new HashMap<>();
	static{
		TEST_CONFIG.put("ATestKey", "ATestValue");
	}
	
	@Test
	public void testLoadConfig(){
		DatabaseUtil mockUtil = Mockito.mock(DatabaseUtil.class);
		try {
			Mockito.when(mockUtil.getDatabaseConfig(null)).thenReturn(TEST_CONFIG);
		} catch (SQLException e) {
			assert false;
		}
		try {
			assert TEST_CONFIG.equals(DefyndianConfig.loadConfig());
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			assert false;
		}
	}
}
