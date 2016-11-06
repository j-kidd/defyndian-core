package defyndian.config;

import defyndian.exception.ConfigInitialisationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BasicConfigTest {

	private static final Map<String, String> confProperties = new HashMap<>(4);
	static{
		confProperties.put("first", "a");
		confProperties.put("second","b");
		confProperties.put("third", "c");
		confProperties.put("fourth","d");
	}

	private static final File configDirectory = new File("node-config");

	private Properties baseProperties;
	
	private final String configName = "basicConfig.conf";

	@Before
	public void setup() throws IOException {
		baseProperties = new Properties();
		baseProperties.setProperty("config.type", ConfigType.BASIC.toString());
		TestUtils.createDirectory(configDirectory);
	}

	@After
	public void cleanup(){
		TestUtils.deleteDirectory(configDirectory);
	}
	
	@Test
	public void constructNew_goodConfig_allPropertiesLoaded() throws ConfigInitialisationException, IOException {
		TestUtils.copyConfigFile(configName, configDirectory);
		DefyndianConfig config = new BasicConfig(configName, baseProperties);
		for( String key : confProperties.keySet() ){
			final String actualValue = config.get(key);
			final String expected = confProperties.get(key);
			assertThat(String.format("Key '%s' incorrect value '%s' expected '%s'", key, actualValue, expected),
					actualValue, equalTo(expected));
		}
	}
}
