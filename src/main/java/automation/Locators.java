package automation;

import automation.utils.FileUtil;
import com.google.gson.JsonObject;
import org.openqa.selenium.By;

public class Locators {

    private static final JsonObject MAP;

    static {
        MAP = FileUtil.readJsonFile("ui/locators_map.json", JsonObject.class);
    }

    public static By findBy(String key){
        JsonObject jsonLocator = MAP.get(key).getAsJsonObject();
        return match(jsonLocator);
    }

    private static By match(JsonObject jsonLocator){
        final String value = jsonLocator.get("value").getAsString();
        final String type = jsonLocator.get("type").getAsString();

        switch (type){
            case "name":
                return By.name(value);
            case "class":
                return By.className(value);
            case "id":
                return By.id(value);
            case "tag":
                return By.tagName(value);
            case "css":
                return By.cssSelector(value);
            case "xpath":
                return By.xpath(value);
            default:
                throw new IllegalArgumentException("no such locator type '" + type + "' for locator '" + value + "'");
        }

    }
}
