package defyndian.config.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import defyndian.config.MapBackedConfig;
import defyndian.exception.ConfigInitialisationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by james on 05/11/16.
 */
public class JsonTreeConfig extends MapBackedConfig {

    private static final String DEFAULT_CONFIG_DIRECTORY = "node-config";
    private static final String CONFIG_DIRECTORY_PROPERTY_KEY = "json.config.directory";
    private static final Pattern configFileRegex = Pattern.compile("\\w+\\.conf");

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonTreeConfig.class);

    public JsonTreeConfig(String name, Properties init) throws ConfigInitialisationException {
        super(name, init);
    }

    @Override
    protected Map<String, String> initialiseConfig() throws ConfigInitialisationException {
        final String configDirectoryName = get(CONFIG_DIRECTORY_PROPERTY_KEY, DEFAULT_CONFIG_DIRECTORY);
        final File configDirectory = Paths.get(configDirectoryName).toFile();
        final Collection<File> configFiles = findConfigFiles(configDirectory);
        final Map<String,String> loadedConfig = new HashMap<>();

        for( File configFile : configFiles ){
            try {
                final String configName = configFile.getName();
                final JsonNode root = mapper.readTree(configFile);
                final Map<String,String> subConfig = getPropertiesFromJsonTree(root).entrySet().stream()
                            .                           collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                for( String configKey : subConfig.keySet() ){
                    final String configValue = subConfig.get(configKey);
                    final String overwrittenValue = loadedConfig.putIfAbsent(configKey, configValue);
                    if( overwrittenValue!=null )
                        logger.warn("Overwriting config key {}. New Value {}. Old value {}", configKey, configValue, overwrittenValue);
                }
            } catch (IOException e) {
                throw new ConfigInitialisationException(String.format("Error reading config in '%s'. Config skipped", configFile.getName()), e);
            }
        }
        return loadedConfig;
    }

    @Override
    public void save() {

    }

    private Map<String,String> getPropertiesFromJsonTree(JsonNode root){
        return parseObject("", root);
    }

    private Map<String,String> parseObject(String key, Object object){
        final Map<String,String> subConfig = new HashMap<>();
        if( object instanceof ObjectNode ){
            subConfig.putAll(parseJsonObject(key, (ObjectNode)object));
        }
        else if( object instanceof ArrayNode ){
            subConfig.putAll(parseJsonArray(key, (ArrayNode) object));
        }
        else{
            subConfig.putAll(parseJsonPrimitive(key, object));
        }
        return subConfig;
    }

    private Map<String,String> parseJsonObject(String key, ObjectNode node){
        final Iterator<Map.Entry<String,JsonNode>> children = node.fields();
        final Map<String,String> subConfig = new HashMap<>();
        while( children.hasNext() ){
            final Map.Entry<String, JsonNode> childEntry = children.next();
            final String childTag;
            if( key.isEmpty() )
                childTag = childEntry.getKey();
            else
                childTag = key +"."+ childEntry.getKey();
            final JsonNode childNode = childEntry.getValue();
            subConfig.putAll(parseObject(childTag, childNode));
        }
        return subConfig;
    }

    private Map<String,String> parseJsonArray(String key, ArrayNode node){
        final Iterator<Map.Entry<String,JsonNode>> children = node.fields();
        final Map<String,String> subConfig = new HashMap<>();
        while( children.hasNext() ){
            final Map.Entry<String, JsonNode> childEntry = children.next();
            final String childTag = key + "."+ childEntry.getKey();
            final JsonNode childNode = childEntry.getValue();
            subConfig.putAll(parseObject(childTag, childNode));
        }
        return subConfig;
    }

    private Map<String,String> parseJsonPrimitive(String key, Object node){
        final Map<String,String> leafNodes = new HashMap<>();
        if( node instanceof TextNode )
            leafNodes.put(key, ((TextNode) node).textValue());
        else if( node instanceof NumericNode )
            leafNodes.put(key, ((NumericNode) node).numberValue().toString());
        else
            logger.error("Found unknown type at key {}. Skipping", key);
        return leafNodes;
    }

    private Collection<File> findConfigFiles(File configDirectory){
        return Arrays.stream(configDirectory.listFiles())
                .filter(s -> configFileRegex.matcher(s.getName()).find())
                .collect(Collectors.toList());
    }

}
