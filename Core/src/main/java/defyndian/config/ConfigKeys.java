package defyndian.config;

import java.util.regex.Pattern;

/**
 * Created by james on 06/11/16.
 */
public enum ConfigKeys {

    json_config_directory,
    mq_host,
    mq_username,
    mq_password,
    mq_exchange,
    mq_queue,
    mq_virtualhost ,
    mq_routingkeys;

    private final String keySeparator = ".";
    private final Pattern keySeparatorPattern = Pattern.compile("_");
    private final String representation;

    ConfigKeys(){
        representation = keySeparatorPattern.matcher(name()).replaceAll(keySeparator);
    }

    public String toString(){
        return representation;
    }
}
