package automation.selenium;

import automation.systemUtils.SystemUtils;
import automation.utils.StringUtil;
import automation.utils.Waiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.awaitility.core.Condition;
import org.openqa.selenium.*;
import automation.conf.EnvConf;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.awaitility.Duration;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static automation.logger.LoggerFactory.LOG;

public class DriverWrapper implements WebDriver {
    private final WebDriver driver;
    private static final Duration WAIT_ELEMENT_TIMEOUT = new Duration(EnvConf.getAsInteger("ui.locator.timeout.sec"), TimeUnit.SECONDS);
    private DriverWrapper(WebDriver driver) {
        this.driver = driver;
    }

    static {
        setWebDriverByOs();
    }

    private static void setWebDriverByOs() {
        String driverPath;
        switch (SystemUtils.match()) {
            case LINUX:
                driverPath = "src/main/resources/driver/linux/chromedriver";
                break;
            case WIN:
                driverPath = "src/main/resources/driver/win/chromedriver.exe";
                break;
            default:
                throw new IllegalStateException("chrome driver didn't set at system properties");
        }

        EnvConf.setChromeWebDriverPath(driverPath);//todo - when using multiple browsers types? check it each test have new instance of class loader
//        LOG.d("'webdriver.chrome.driver=" + EnvConf.getChromeWebDriverPath());
    }

    public static DriverWrapper open(Browser browser, File downloadsFolder) {
        switch (browser) {
            case FIREFOX:
                return createFireFoxInst();
            case IE:
                return createIEInst();
            case CHROME:
                return createChromeInst(downloadsFolder);
            default:
                throw new IllegalArgumentException("'" + browser + "'no such browser type");
        }
    }

    private static DriverWrapper createFireFoxInst() {
        FirefoxOptions options = new FirefoxOptions();
        options.setAcceptInsecureCerts(true);
        options.setHeadless((EnvConf.getAsBoolean("selenium.headless")));
        options.addArguments("--window-size=1920,1080");
        FirefoxDriver firefoxDriver = new FirefoxDriver(options);
        return new DriverWrapper(firefoxDriver);
    }

    public File getScreenshotAsFile() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    }

    private static DriverWrapper createIEInst() {
        InternetExplorerOptions options = new InternetExplorerOptions();
        InternetExplorerDriver internetExplorerDriver = new InternetExplorerDriver(options);
        return new DriverWrapper(internetExplorerDriver);
    }

    private static DriverWrapper createChromeInst(File downloadsFolder) {
        Map<String, Object> pref = new Hashtable<>();
        pref.put("download.default_directory", downloadsFolder);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", pref);
        options.setAcceptInsecureCerts(true);
        options.setHeadless(EnvConf.getAsBoolean("selenium.headless"));
        options.addArguments("--window-size=" + EnvConf.getProperty("selenium.window_size"));

        options.addArguments("--lang=" + EnvConf.getProperty("selenium.locale"));
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.SEVERE);
        options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        ChromeDriverService service = ChromeDriverService.createDefaultService();
//        ChromeDriver chromeDriver = new ChromeDriver(service, options);
        ChromeDriver chromeDriver1 = new ChromeDriver(service);
//        enableHeadlessDownload(service, chromeDriver1, downloadsFolder);
        return new DriverWrapper(chromeDriver1);
    }

    public boolean waitForElmContains(WebElement element, String text) {
        return waitForElmContains(WAIT_ELEMENT_TIMEOUT, element, text);
    }

    public boolean waitForElmContains(Duration duration, By by, String text) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, duration.getTimeUnit().toSeconds(duration.getValue()));
            return wait.until(ExpectedConditions.textToBePresentInElementLocated(by, text));
        } catch (TimeoutException e) {
            LOG.e("failed waiting to locator=[%s] text=[%s] for timeout [%s] secs",
                    by, text, duration);
            return false;
        }
    }

    private boolean waitForElmContains(Duration duration, WebElement element, String text) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, duration.getTimeUnit().toSeconds(duration.getValue()));
            return wait.until(ExpectedConditions.textToBePresentInElement(element, text));
        } catch (TimeoutException e) {
            LOG.e("failed waiting to locator=[%s] text=[%s] for timeout [%s] secs",
                    element, text, duration);
            return false;
        }
    }

    public WebElement waitElmByText(Duration durationInSec, By parentBy, By childrenBy, String text) {
        List<WebElement> elements = waitForChildrenElm(durationInSec, parentBy, childrenBy);
        WebElement element = null;
        if (elements != null) {
            LOG.d("elements [%s]", elements);
            element = elements.stream().filter(elm -> elm.getText().contains(text)).findFirst().orElse(null);
        }
        return element;
    }

    public List<WebElement> waitForChildrenElm(Duration durationInSec, By parentBy, By childrenBy) {
        Condition<List<WebElement>> condition = () -> {
            List<WebElement> elements = getChildren(parentBy, childrenBy);
            return (elements.isEmpty()) ? null : elements;
        };

        return Waiter.waitCondition(durationInSec, condition);
    }

    public List<WebElement> waitForChildrenElm(Duration durationInSec, WebElement parentElm, By childrenBy) {
        Condition<List<WebElement>> condition = () -> {
            List<WebElement> elements = getChildren(parentElm, childrenBy);
            return (elements.isEmpty()) ? null : elements;
        };

        return Waiter.waitCondition(durationInSec, condition);
    }

    private List<WebElement> getChildren(WebElement parentElm, By childrenBy) {
        return parentElm.findElements(childrenBy);
    }

    private List<WebElement> getChildren(By parentBy, By childrenBy) {
        return waitForElmVisibility(parentBy).findElements(childrenBy);
    }

    private WebElement getChild(By parentBy, By childrenBy) {
        return driver.findElement(parentBy).findElement(childrenBy);
    }

    public WebElement waitForElmContainsText(final Duration durationInSec, final By parentBy, final By childrenBy,
                                             final String text) {
        Condition<WebElement> condition = () -> waitElmByText(durationInSec, parentBy, childrenBy, text);
        return Waiter.waitCondition(durationInSec, condition);
    }

    private static void enableHeadlessDownload(ChromeDriverService service, ChromeDriver chromeDriver, File downloadsFolder) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            Map<String, Object> commandParams = new HashMap<>();
            commandParams.put("cmd", "Page.setDownloadBehavior");
            Map<String, String> params = new HashMap<>();
            params.put("behavior", "allow");
            params.put("downloadPath", downloadsFolder.getAbsolutePath());
            commandParams.put("params", params);
            ObjectMapper objectMapper = new ObjectMapper();
            String command = objectMapper.writeValueAsString(commandParams);
            String url = service.getUrl() + "/session/" + chromeDriver.getSessionId() + "/chromium/send_command";
            HttpPost request = new HttpPost(url);
            request.addHeader("content-type", "application/json");
            request.setEntity(new StringEntity(command));
            CloseableHttpResponse response = httpClient.execute(request);
            String responseContent = StringUtil.convert(response.getEntity().getContent(), 10);
            LOG.i("enable download response=[%s], status code=[%d]", responseContent, response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            LOG.e(e, "failed to send command=[age.setDownloadBehavior] to chrome server");
        }
    }
    @Override
    public void get(String s) {
        driver.get(s);
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return driver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return driver.findElement(by);
    }

    @Override
    public String getPageSource() {
        return driver.getPageSource();
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public void quit() {
        driver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return driver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return driver.navigate();
    }

    @Override
    public Options manage() {
        return driver.manage();
    }

    public WebElement waitForElmClickable(By by) {
        return waitForElmClickable(Duration.TEN_SECONDS, by);
    }

    public WebElement waitForElmClickable(Duration duration, By by) {
        WebDriverWait wait = new WebDriverWait(driver, duration.getTimeUnit().toSeconds(duration.getValue()));
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    public WebElement waitForElmVisibility(By by) {
        return waitForElmVisibility(Duration.TEN_SECONDS, by);
    }

    public WebElement waitForElmVisibility(Duration duration, By by) {
        WebDriverWait wait = new WebDriverWait(driver, duration.getTimeUnit().toSeconds(duration.getValue()));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }
}
