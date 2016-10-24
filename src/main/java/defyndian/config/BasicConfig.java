package defyndian.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import defyndian.exception.ConfigInitialisationException;
import defyndian.exception.DefyndianDatabaseException;
import defyndian.exception.DefyndianMQException;
import defyndian.exception.MalformedConfigFileException;

/**	
 * Basic config is a simple file based implementation of the
 * DefyndianConfig. It uses files in a directory specified in
 * the local conf to store config options; each file is a namespace.
 * If no config directory is specified config.d in the local dir is used
 * @author james
 *
 */
public class BasicConfig extends MapBackedConfig{

	private static final String CONFIG_FILE_EXT = ".conf";
	private static final String CONFIG_DIRECTORY_KEY = "config.d";
	private static final String COMMENT_CHAR = "#";
	private static final Pattern KEY_VALUE_SEPARATORS = Pattern.compile("[=:\\-\\s]+");

	private File configDirectory;
	
	public BasicConfig(String name, Properties init) throws ConfigInitialisationException{
		super(name, init);
	}
	
	@Override
	protected final Map<String, String> initialiseConfig() throws ConfigInitialisationException {
		String configDirectoryName = get(CONFIG_DIRECTORY_KEY);
		File configDir;
		if( configDirectoryName!=null ){
			if ((configDir=new File(configDirectoryName)).exists() )
				configDirectory = configDir;
			else
				throw new ConfigInitialisationException("Config directory " + configDir + " doesn't exist");
		}
		else
			configDirectory = Paths.get("").toAbsolutePath().toFile();

		return initialiseConfig(configDirectory);
	}

	protected Map<String,String> initialiseConfig(File configDirectory) throws ConfigInitialisationException {
		File[] filesInConfigDir = configDirectory.listFiles();
		if( filesInConfigDir==null ){
			throw new ConfigInitialisationException("Config directory is not a valid directory - " + configDirectory);
		}
		Map<String, String> conf = new HashMap<>();
		for( File confFile : filesInConfigDir ){
			try{
				if( confFile.getName().endsWith(CONFIG_FILE_EXT) )
					conf.putAll(readConfigFile(confFile.getAbsoluteFile()));
			} catch( MalformedConfigFileException | IOException e ){
				throw new ConfigInitialisationException(e);
			}
		}
		return conf;
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}
	
	private Map<String, String> readConfigFile(File singleNamespaceFile) throws IOException, MalformedConfigFileException{
		Map<String, String> conf = new HashMap<>();
		BufferedReader reader = new BufferedReader(new FileReader(singleNamespaceFile));
		String line;
		while( (line=reader.readLine()) != null ){
			String trimmedLine = line.trim();
			if( isCommentLine(trimmedLine) | isEmptyLine(trimmedLine) )
				continue;
			
			String[] keyValue = KEY_VALUE_SEPARATORS.split(trimmedLine);
			if( keyValue.length != 2 ){
				throw new MalformedConfigFileException("Could not parse line '" + line+"' in file " + singleNamespaceFile.getAbsolutePath());
			}
			String key = keyValue[0];
			String value = keyValue[1];
			conf.put(key,  value);
		}
		reader.close();
		return conf;
	}
	
	private boolean isCommentLine(String line){
		return line.startsWith(COMMENT_CHAR);
	}
	
	private boolean isEmptyLine(String line){
		return line.isEmpty();
	}
	
}
