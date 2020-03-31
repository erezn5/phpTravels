package automation.conf;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import java.util.Map;

public class EnvConf {

    private static Configuration properties;

    static {
        load();
    }

    private EnvConf() {}

    private static void load() {
        Configurations configs = new Configurations();
        try {
            properties = configs.properties(getEnvPropFilePath());
            for(Map.Entry p : System.getProperties().entrySet()){
                String key = (String)p.getKey();
                properties.setProperty(key , p.getValue());
            }
        } catch (ConfigurationException e) {
            System.err.println("failed to load '" + getEnvPropFilePath() + "' resource file");
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key){
        return properties.getString(key);
    }

    public static void setProperty(String key, String value){
        properties.addProperty(key, value);
    }

    public static String getEnvProperty(String key , String defaultValue){
        String value = System.getenv(key);
        return (value == null) ? defaultValue : value;
    }

    public static int getAsInteger(String key){
        return Integer.parseInt(getProperty(key));
    }

    public static boolean getAsBoolean(String key){
        return Boolean.parseBoolean(getProperty(key));
    }

    public static void setChromeWebDriverPath(String driverPath){
        System.setProperty("webdriver.chrome.driver" , driverPath);
        properties.setProperty("webdriver.chrome.driver" , driverPath);
    }

    public static String getChromeWebDriverPath(){
        return properties.getString("webdriver.chrome.driver");
    }

    private static String getEnvPropFilePath(){
        String envPath = System.getProperty("env.conf");
        envPath = (envPath == null) ? "env/env.properties" : envPath;
        return envPath;
//        return (envPath == null) ? "C:\\Git\\seleniumProject\\src\\main\\resources\\env\\env.properties" : envPath;
    }

    public static String getSetupFilePath(){
        return properties.getString("env.setup");
    }

    public static boolean isDevelopmentEnv(){
        return getEnvName() == EnvName.DEVELOPMENT;
    }

    private static EnvName getEnvName(){
        return EnvName.valueOf(getProperty("env.name"));
    }

    public static String getDefaultTimeZone(){
        return getProperty("env.timezone.id");
    }

}
