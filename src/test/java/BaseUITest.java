import automation.conf.EnvConf;
import automation.selenium.Browser;
import automation.selenium.DriverWrapper;
import automation.utils.FileUtil;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import static automation.logger.LoggerFactory.LOG;

public class BaseUITest {
    protected DriverWrapper driverWrapper;
    private static final Browser BROWSER = Browser.valueOf(EnvConf.getProperty("ui.browser.type"));
    private static final File SCREENSHOTS_FOLDER = new File(EnvConf.getProperty("test_output.screenshots.folder"));
    private static final SimpleDateFormat FOLDER_NAME_FORMAT = new SimpleDateFormat("dd_MM_HH_mm_ss");
    private static final File DOWNLOADS_FOLDER = new File(EnvConf.getProperty("workspace.tests.downloads"));

    static {
        if(!SCREENSHOTS_FOLDER.exists()){
            FileUtil.createFolder(SCREENSHOTS_FOLDER , true);
        }
    }

    @BeforeClass
    public final void baseUiSetup(){
        driverWrapper = DriverWrapper.open(BROWSER, DOWNLOADS_FOLDER);
    }

    @AfterClass
    public final void baseUiTearDown(){
        if(driverWrapper != null){
            driverWrapper.quit();
        }
    }

    @AfterMethod
    public void doAfterTestUnit(ITestResult result){
        if(!(result.isSuccess() || EnvConf.isDevelopmentEnv())){
            takeScreenShot(result.getName());
        }
    }

    protected void takeScreenShot(String filePrefix){
        File dest = new File(SCREENSHOTS_FOLDER , filePrefix + "_" + FOLDER_NAME_FORMAT.format(new Date()) + ".png");
        takeScreenShot(dest);
    }

    private void takeScreenShot(File destFile){
        File scrFile = driverWrapper.getScreenshotAsFile();
        Path src = Paths.get(scrFile.toURI());
        Path dest = Paths.get(destFile.toURI());
        try {
            Files.copy(src, dest , StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[[ATTACHMENT|" + destFile.getAbsolutePath() + "]]");
        } catch (IOException e) {
            LOG.e(e ,"Failed to save screen shot at file: " + destFile.getName());
        }
    }
}
