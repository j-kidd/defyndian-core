package defyndian.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import defyndian.config.DefyndianConfig;

public class DefyndianNodeTest {

	private final String TEST_CONFIG_KEY = "TESTING-KEY";
	private final String TEST_CONFIG_VALUE = "TESTING-VALUE";
	private final String TEST_CONFIG_DEFAULT_VALUE = "TESTING-NOT-A-VALUE";
	
	private static final Map<String,String> TEST_CONFIG = new HashMap<>();
	static{
		TEST_CONFIG.put("ATestKey", "ATestValue");
	}
	
	@Test
	public void testConfig() throws FileNotFoundException, SQLException, IOException{
		DefyndianConfig config;
		config = DefyndianConfig.loadConfig();
		assert config.get(TEST_CONFIG_KEY).equals(TEST_CONFIG_VALUE);
		assert config.get(null, TEST_CONFIG_DEFAULT_VALUE).equals(TEST_CONFIG_DEFAULT_VALUE);
	}
	
	
}
