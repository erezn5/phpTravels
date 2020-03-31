package automation.systemUtils;

public class SystemUtils {
    private static final String OS_NAME = System.getProperty("os.name");

    private SystemUtils() { }
    public static OsType match(){
        System.out.println("'os.name' -> " + OS_NAME);
        if(OS_NAME.toLowerCase().contains("win")){
            return OsType.WIN;
        }else if(OS_NAME.toLowerCase().contains("linux")){
            return OsType.LINUX;
        }else if(OS_NAME.toLowerCase().contains("os x")){
            return OsType.OS_X;
        }else {
            throw new IllegalArgumentException("'" + OS_NAME + "' didn't match to any os type enum");
        }
    }

    public enum OsType{
        WIN,
        LINUX,
        OS_X
    }
}

