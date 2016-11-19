package defyndian.config;

import defyndian.config.json.JsonTreeConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by james on 06/11/16.
 */
public class TestUtils {

    public static void deleteDirectory(File configDirectory){
        final File configDir = configDirectory;
        if( configDir.isDirectory() ){
            Arrays.stream(configDir.listFiles())
                    .forEach(File::delete);
            configDir.delete();
        }
    }

    public static void createDirectory(File directory){
        if( directory.isDirectory() )
            deleteDirectory(directory);
        directory.mkdir();
    }

    protected static void copyConfigFile(String configFile, File configDirectory) throws IOException {
        Files.copy( TestUtils.class.getResourceAsStream("/"+configFile),
                    configDirectory.toPath().resolve(configFile));
    }


}
