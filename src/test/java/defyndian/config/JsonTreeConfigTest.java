package defyndian.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import defyndian.config.json.JsonTreeConfig;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by james on 06/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonTreeConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(JsonTreeConfigTest.class);

    @Mock
    private ObjectMapper mapper;

    private static final File configDirectory = new File("node-config");

    private Properties baseProperties;

    private final Map<String,String> goodConfigProperties = new HashMap<String,String>(){{
        put("topLevelA.secondLevelA.leafA", "valueA");
        put("topLevelA.secondLevelA.leafB", "valueB");
        put("topLevelA.secondLevelB.leafC", "valueC");
        put("topLevelB.secondLevelC.leafD", "valueD");
        put("topLevelB.secondLevelC.leafE", "valueE");
        put("topLevelB.secondLevelD.leafF", "valueF");
    }};

    @Before
    public void setup(){
        baseProperties = new Properties();
        baseProperties.put("config.type", ConfigType.JSON.toString());
        TestUtils.createDirectory(configDirectory);
    }

    @After
    public void cleanup(){
        TestUtils.deleteDirectory(configDirectory);
    }

    @Test
    public void constructNew_goodConfig_allKeysAccessible() throws Exception {
        final String configFile = "jsonConfig.conf";
        TestUtils.copyConfigFile(configFile, configDirectory);
        final JsonTreeConfig target = new JsonTreeConfig(configFile, baseProperties);

        goodConfigProperties.keySet().stream()
                .forEach(
                        key -> {
                            final String actualValue = target.get(key);
                            final String expected = goodConfigProperties.get(key);
                            assertThat(String.format("Key '%s' incorrect value '%s' expected '%s'", key, actualValue, expected),
                                    expected, equalTo(actualValue));
                        }
                );
    }


}
