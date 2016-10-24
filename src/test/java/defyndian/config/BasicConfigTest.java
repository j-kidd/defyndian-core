package defyndian.config;

import defyndian.exception.ConfigInitialisationException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BasicConfigTest {

	private static final Map<String, String> confProperties = new HashMap<>(4);
	static{
		confProperties.put("first", "a");
		confProperties.put("second","b");
		confProperties.put("third", "c");
		confProperties.put("fourth","d");
	}
	
	private final String configName = "BasicConfigTest";
	
	@Before
	public void setup(){

	}
	
	@Test
	public void testPropertiesLoaded() throws ConfigInitialisationException {
		DefyndianConfig config = DefyndianConfig.getConfig(configName);
		for( String key : confProperties.keySet() ){
			assertEquals(confProperties.get(key), config.get(key));
		}
	}
}
