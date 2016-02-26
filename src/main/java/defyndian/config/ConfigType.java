package defyndian.config;

import java.lang.reflect.InvocationTargetException;

public enum ConfigType {

	BASIC(BasicConfig.class), MYSQL(MysqlConfig.class);
	
	private final Class<? extends DefyndianConfig> configType;
	
	private ConfigType(Class<? extends DefyndianConfig> configType){
		this.configType = configType;
	}
	
	public final DefyndianConfig getConfig(String name){
		try {
			return configType.getConstructor(String.class).newInstance(name);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not instantiate a config class, should never happen", e);
		}
	}
}
