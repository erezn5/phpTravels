package automation.logger;

import org.testng.ITestContext;
import org.testng.Reporter;
import automation.conf.EnvConf;
import org.apache.log4j.Level;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestLogger extends LoggerFormat {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of(EnvConf.getDefaultTimeZone());
    private final ThreadLocal<Map<String , List<String>>> testsLogMap = new ThreadLocal<>();

    public void info(ITestContext context , String messageFormat , Object...args) {
        String msg = handleFormatMsg(messageFormat , args);
        super.info(msg);
        log(Level.INFO , context ,  msg);
    }

    public void error(ITestContext context , Throwable t , String messageFormat , Object...args) {
        String msg = handleFormatMsg(messageFormat , args);
        super.error(msg , t);
        log(Level.ERROR , context , msg , t);
    }

    public void error(ITestContext context , String messageFormat , Object...args) {
        String msg = handleFormatMsg(messageFormat , args);
        super.error(msg);
        log(Level.ERROR , context , msg);
    }

    public void warn(ITestContext context , String messageFormat , Object...args) {
        String msg = handleFormatMsg(messageFormat , args);
        super.warn(msg);
        log(Level.WARN , context , msg);
    }

    public void warn(ITestContext context , Throwable t ,String messageFormat , Object...args) {
        String msg = handleFormatMsg(messageFormat , args);
        super.warn(msg , t);
        log(Level.WARN , context , msg , t);
    }

    public void debug(ITestContext context , String messageFormat , Object...args) {
        String msg = handleFormatMsg(messageFormat , args);
        super.debug(msg);
        if(LoggerFactory.isDebug()){
            log(Level.DEBUG , context , msg);
        }
    }

    public void debug(ITestContext context , Throwable t , String messageFormat , Object...args) {
        String msg = handleFormatMsg(messageFormat , args);
        super.debug(msg , t);
        if(LoggerFactory.isDebug()){
            log(Level.DEBUG , context , msg , t);
        }
    }

    private void log(Level level , ITestContext context , Object message , Throwable t){
        message = String.format("%s\n%s" , message , throwableToString(t));
        log(level , context , message);
    }

    private void log(Level level , ITestContext context , Object message){
        if(!EnvConf.isDevelopmentEnv()){
            Map<String , List<String>> logsMap = getLogsMap();
            if(!logsMap.containsKey(context.getName())){
                logsMap.put(context.getName() , new ArrayList<>());
            }

            logsMap.get(context.getName()).add(formatMsg(message , level));
            if(level.toInt() != Level.DEBUG.toInt()){
                Reporter.log((String) message , 0);
            }
        }
    }

    private static String formatMsg(Object message , Level level){
        LocalDateTime dateTime = LocalDateTime.now(DEFAULT_ZONE_ID);
        return String.format("[%s][%s]%s" ,
                TIMESTAMP_FORMAT.format(dateTime) ,
                level , message);
    }

    public List<String> getAndDeleteLogsByTest(String testName){
        return getLogsMap().remove(testName);
    }

    private Map<String , List<String>> getLogsMap(){
        if(testsLogMap.get() == null){
            testsLogMap.set(new HashMap<>());
        }
        return testsLogMap.get();
    }


    private static String throwableToString(Throwable t){
        StringBuilder builder = new StringBuilder();
        for(StackTraceElement traceElement : t.getStackTrace()){
            builder.append(traceElement.toString()).append('\n');
        }
        return builder.toString();
    }


    private static String handleFormatMsg(String message, Object...args){
        if(args.length == 0 && message.contains("%")){
            message = message.replaceAll("%", "%%");
        }
        return String.format(message , args);
    }
}

