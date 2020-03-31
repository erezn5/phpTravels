package automation.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static automation.logger.LoggerFactory.LOG;

public class FileUtil {

    private static final Gson GSON = new GsonBuilder().create();
//    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());


    private FileUtil() { }

    //    public static Properties loadProperties(File file){
//        if(file.exists()){
//            try(FileInputStream fis = new FileInputStream(file)){
//                Properties properties = new Properties();
//                properties.load(fis);
//                System.out.println(file.getName() + " file load configuration successfully!");
//                return properties;
//            } catch (IOException e) {
//                System.err.print("Failed to load configuration from: " + file.getName() + ", cause: " + e.getMessage());
//            }
//        }
//        return null;
//    }

    public static <T> T readJsonFile(String relativePath , Class<T> clazz){
        try(InputStream ips = clazz.getClassLoader().getResourceAsStream(relativePath);
            InputStreamReader ipsr = new InputStreamReader(ips)){
            JsonReader reader = new JsonReader(ipsr);
            return  GSON.fromJson(reader , clazz);
        }catch (IOException e){
            System.err.println("Failed to convert resource file '" + relativePath + "' stream to properties, cause: " + e.getMessage());
            return null;
        }
    }

    public static <T> T fromJsonList(String stringToParse, Class<T> clazz) {
        return GSON.fromJson(stringToParse, clazz);
    }

    public static boolean delete(File file){
        boolean success = file.delete();
        LOG.i("File was deleted =[%b]", success);
        return success;
    }

    public static String removeExtension(String fileName){
        if(fileName.indexOf(".")>0)
            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        return fileName;
    }

    public static <T> T readJsonFile(File path , Class<T> clazz){
        try(InputStream ips = new FileInputStream(path);
            InputStreamReader ipsr = new InputStreamReader(ips)){
            JsonReader reader = new JsonReader(ipsr);
            return  GSON.fromJson(reader , clazz);
        }catch (IOException e){
            System.err.println("Failed to convert resource file '" + path.getAbsolutePath() + "' stream to properties, cause: " + e.getMessage());
            return null;
        }
    }


    public static Properties createPropertiesFromResource(Class clazz , String relativePath) {
        try(InputStream ips = clazz.getClassLoader().getResourceAsStream(relativePath)){
            Properties properties = new Properties();
            properties.load(ips);
            return properties;
        }catch (IOException e){
            System.err.println("Failed to convert resource'" + relativePath + "'stream to properties, cause: " + e.getMessage());
            return null;
        }
    }

    public static void createFolder(File folder , boolean recursive){
        if(folder.exists() && folder.isDirectory()){
            LOG.i(folder.getName() + " directory already exist");
        }else if((recursive ? folder.mkdirs() : folder.mkdir())){
            LOG.i(folder.getName() + " directory created successfully");
        }else{
            LOG.error("failed to create '" + folder.getName() + "' directory");
        }
    }

//    public static <T> T fromYAML(File yamlFile , Class<T> classType) {
//        try {
//            return YAML_MAPPER.readValue(yamlFile , classType);
//        } catch (Exception e) {
//            LOG.e(e , "failed to deserialize yaml file=[%s] to object type=[%s]" , yamlFile.getName() , classType.getSimpleName());
//            throw new IllegalArgumentException(e);
//        }
//    }

    public static boolean createNewFile(File file){
        if(file.exists()){
            LOG.i("file=[%s] already exist" , file.getAbsolutePath());
            return true;
        }else {
            try {
                if(file.createNewFile()){
                    LOG.i("file=[%s] created successfully" , file.getAbsolutePath());
                    return true;
                }else{
                    LOG.e("failed to create file=[%s]" , file.getAbsolutePath());
                }
            } catch (IOException e) {
                LOG.e(e ,"error occur creating file=[%s]" , file.getAbsolutePath());
            }
        }
        return false;
    }

    public static void writeToFile(String filePath , List<String> lines) {
        try(FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)){
            for(String line : lines){
                bufferedWriter.write(line);
                bufferedWriter.write('\n');
            }
            bufferedWriter.flush();
        }catch (Exception e){
            LOG.e(e ,"failed write to file=[%s]" , filePath);
        }
    }

    public static void writeToFile(String filePath , byte[] content) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(content);
            fos.flush();
        }catch (Exception e){
            LOG.e(e ,"failed write to file=[%s]" , filePath);
        }
    }

    public static void serializeObj(File serObjToFile , Object serObj){
        if(FileUtil.createNewFile(serObjToFile)){
            try (FileOutputStream fos = new FileOutputStream(serObjToFile);
                 ObjectOutputStream out = new ObjectOutputStream(fos)){
                out.writeObject(serObj);
            } catch (Exception e) {
                LOG.e(e , "failed to serialize mock-device object=[%s] to file=[%s]" , serObj , serObjToFile.getAbsolutePath());
            }
        }
    }

//    public static boolean waitFileExist(File f, Duration timeout, Duration interval){
//        Condition<Boolean> condition = f::exists;
//        return Waiter.waitCondition(timeout, condition, interval);
//    }

    public static Object deserializeObj(File serObjToFile){
        if(serObjToFile.exists()){
            try (FileInputStream fis = new FileInputStream(serObjToFile);
                 ObjectInputStream in = new ObjectInputStream(fis)){
                return in.readObject();
            } catch (Exception e) {
                LOG.e(e , "failed to deserialize mock-device object from file=[%s]" , serObjToFile.getAbsolutePath());
            }
        }else{
            LOG.w("failed to deserialize mock-device object=[%s] file=[%s] don't exist" , serObjToFile.getAbsolutePath());
        }
        return null;
    }

    public static <T> void writeJsonToFile(T instance, String filePath) {
        String json = GSON.toJson(instance, instance.getClass());
        writeToFile(filePath, json.getBytes());
    }

}

