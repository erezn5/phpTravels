package automation.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static automation.logger.LoggerFactory.LOG;

public class StringUtil {

    public static final String VERSION_REGEX = "(\\d+\\.){3}\\d+";
    public static final String CRLF = "\r\n";
    public static final String LF = "\n";
    public static final String EMPTY = StringUtils.EMPTY;

    private StringUtil(){ }

    public static String convert(InputStream ips , long timeoutMilliSec) {

        long timeEnd = System.currentTimeMillis() + timeoutMilliSec;
        long zeroBytesTimeoutMilli = (long) Math.ceil(((double) timeoutMilliSec/10));
//        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StringBuilder builder = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(ips);
             BufferedReader br = new BufferedReader(isr)){
            int nRead;
            byte[] data = new byte[1024];

            String line;
            while (timeEnd > System.currentTimeMillis() &&
                    !Thread.currentThread().isInterrupted() &&
                    zeroBytesTimeoutMilli > 0) {
                if(ips.available() > 0){
//                    while (!Thread.currentThread().isInterrupted() &&
//                            (nRead = ips.read(data, 0, data.length)) != -1){
//                        buffer.write(data, 0, nRead);
//                    }
                    while (!Thread.currentThread().isInterrupted() &&
                            (line = br.readLine()) != null) {
                        builder.append(line)
                                .append("\n");
                        LOG.debug(line);
                    }
                    break;
                }else{
                    long current = System.currentTimeMillis();
                    Thread.sleep(200);
                    zeroBytesTimeoutMilli -= System.currentTimeMillis() - current;
                }
            }
        }catch (Exception e){
            LOG.w(e , "failed to read input stream");
        }
//        return flushBufferToString(buffer);
        return builder.toString();
    }

    private static String flushBufferToString(ByteArrayOutputStream buffer){
        try {
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            buffer.close();
            return (byteArray.length == 0) ? null : new String(byteArray, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.e(e , "failed to close buffer");
            return null;
        }
    }

    public static String findFirst(String src , String regex){
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(src);
        if (m.find() && m.end()-m.start() > 0) {
            return src.substring(m.start() , m.end());
        }
        return null;
    }

    public static String replaceAllDotsToEscapeChar(String pattern){
        return pattern.replaceAll("\\." , "\\\\\\.");
    }

    public static String listToString(Collection<?> collection){
        if(collection == null){
            return "";
        }else{
            return collection.stream().map(Object::toString).collect(Collectors.joining(","));
        }
    }

    public static boolean versionBigger(String version1 , String version2){
        if(!(isValidVersion(version1) && isValidVersion(version2))){
            throw new IllegalArgumentException("at least on version parameter is invalid, version1=["
                    + version1 + "], version2=[" + version2 + "]");
        }
        String[] versionDig1 = version1.split("\\.");
        String[] versionDig2 = version2.split("\\.");
        int min = Math.min(versionDig1.length, versionDig2.length);
        for(int i = 0 ; i < min ; i++){
            int verDig1 = Integer.parseInt(versionDig1[i]);
            int verDig2 = Integer.parseInt(versionDig2[i]);
            if(verDig2 > verDig1){
                return false;
            }else if(verDig1 > verDig2){
                return true;
            }
        }
        return false;
    }

    public static boolean isValidVersion(String version){
        return !(version == null || findFirst(version, VERSION_REGEX) == null);
    }
}

