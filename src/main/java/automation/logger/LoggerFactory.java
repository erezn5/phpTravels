package automation.logger;

import automation.utils.FileUtil;
import org.apache.log4j.PropertyConfigurator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import automation.conf.EnvConf;

public class LoggerFactory {

    private static final Properties log4jProperties;
    private static final AtomicReference<LogLevel> logLevel = new AtomicReference<>(LogLevel.UNKNOWN);
    public static final LoggerFormat LOG = new LoggerFormat();
    public static final TestLogger TESTS_LOG = new TestLogger();

    static {
        String log4jPath = EnvConf.getProperty("conf.log4j");
        if(log4jPath == null){
            log4jProperties = null;
            System.err.println("log4j.properties file don't exist!");
        }else{
            Properties properties = FileUtil.createPropertiesFromResource(LoggerFactory.class , log4jPath);
            if(properties == null){
                log4jProperties = null;
                throw new IllegalStateException("Failed to load '" + log4jPath + "' file");
            }else{
                log4jProperties = properties;
                PropertyConfigurator.configure(log4jProperties);
            }
        }
    }

    private LoggerFactory() {
    }

    private static LogLevel getLogLevel(){
        if(log4jProperties != null &&
                logLevel.get() == LogLevel.UNKNOWN){
            String rootLoggerLine = log4jProperties.getProperty("log4j.rootLogger");
            String level = rootLoggerLine.split(",")[0].trim();
            logLevel.set(LogLevel.valueOf(level));
        }
        return logLevel.get();
    }

    public static boolean isDebug(){
        return getLogLevel() == LogLevel.DEBUG;
    }

}

