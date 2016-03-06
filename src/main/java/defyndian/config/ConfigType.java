package defyndian.config;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import defyndian.exception.ConfigInitialisationException;

public enum ConfigType {

	BASIC(BasicConfig.class), MYSQL(MysqlConfig.class);
	
	private final Class<? extends DefyndianConfig> configType;
	
	private ConfigType(Class<? extends DefyndianConfig> configType){
		this.configType = configType;
	}
	
	public final DefyndianConfig getConfig(String name, Properties init) throws ConfigInitialisationException{
		try {
			return configType.getDeclaredConstructor(String.class, Properties.class).newInstance(name, init);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not instantiate a config class, should never happen", e);
		} catch (InvocationTargetException i){
			throw new ConfigInitialisationException(i.getCause());
		}
	}
}
