package defyndian.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import defyndian.config.MysqlConfig;
import defyndian.exception.ConfigInitialisationException;

public class BasicConfigTest {

	private static final Map<String, String> testLocalProperties = new HashMap<>(4);
	static{
		testLocalProperties.put("config.db.host", "localhost");
		testLocalProperties.put("config.db.username",	"homeauto");
		testLocalProperties.put("config.db.password", "defyndian");
		testLocalProperties.put("config.db.database",	"config");
	}
	
	private static final Map<String, String> testGlobalBasicConfig = new HashMap<>(4);
	static{
		testGlobalBasicConfig.put("first", "a");
		testGlobalBasicConfig.put("second",	"b");
		testGlobalBasicConfig.put("third", "c");
	}
	
	private static final Map<String, String> testNodeBasicConfig = new HashMap<>(4);
	static{
		testNodeBasicConfig.put("fourth", "x");
		testNodeBasicConfig.put("fifth",	"y");
		testNodeBasicConfig.put("sixth", "z");
	}
	
	private final String configName = "BasicConfigTest";
	
	private static File goodProperties;
	private static File badProperties;
	private static File basicGlobalProperties;
	private static File basicNodeProperties;
	
	public BasicConfigTest() {
	}
	
	@BeforeClass
	public static void setupFiles() throws URISyntaxException{
		goodProperties = 		new File(BasicConfigTest.class.getClassLoader().getResource("goodConfig.properties").toURI());
		badProperties = 		new File(BasicConfigTest.class.getClassLoader().getResource("badConfig.properties").toURI());
		basicGlobalProperties = new File(BasicConfigTest.class.getClassLoader().getResource("basicGlobalConfig.properties").toURI());
		basicNodeProperties = 	new File(BasicConfigTest.class.getClassLoader().getResource("basicNodeConfig.properties").toURI());
	}
	
	@Test
	public void testPropertiesLoaded() throws ConfigInitialisationException {
		DefyndianConfig goodConfig = DefyndianConfig.getConfig(configName, goodProperties);
		for( String key : testLocalProperties.keySet() ){
			assertEquals(goodConfig.getFromLocal(key), testLocalProperties.get(key));
		}
	}
	
	@Test
	public void testBadPropertiesLoaded() {
		try{
			DefyndianConfig config = DefyndianConfig.getConfig(configName, badProperties);
			System.out.println(config.getFromLocal("config.basic.d"));
			System.out.println(config);
		} catch ( ConfigInitialisationException e){
			return;
		}
		
		throw new IllegalStateException("Should have thrown exception on bad config");
	}
	
	@Test
	public void testGlobalNamespaceLoaded() throws ConfigInitialisationException {
		DefyndianConfig goodConfig = DefyndianConfig.getConfig(configName, basicGlobalProperties);
		for( String key : testLocalProperties.keySet() ){
			assertEquals(goodConfig.get(key), testGlobalBasicConfig.get(key));
		}
	}
	
	@Test
	public void testNodeNamespaceLoaded() throws ConfigInitialisationException {
		DefyndianConfig goodConfig = DefyndianConfig.getConfig(configName, basicNodeProperties);
		for( String key : testLocalProperties.keySet() ){
			assertEquals(goodConfig.get(key), testNodeBasicConfig.get(key));
		}
	}
	
	
}
