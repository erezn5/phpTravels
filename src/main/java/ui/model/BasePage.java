package ui.model;

import automation.conf.EnvConf;
import automation.selenium.DriverWrapper;
import org.openqa.selenium.By;

import static automation.logger.LoggerFactory.LOG;

public class BasePage extends PageElements {
    private final String url;
    protected final static String URL_ADDRESS= EnvConf.getProperty("ui.target.url");
    private final By navigationVerifier;
    public BasePage(DriverWrapper driver, String path, By navigationVerifier){
        super(driver);
        this.url = URL_ADDRESS + "/" + path;
        this.navigationVerifier = navigationVerifier;
    }

    private void navigate(){
        driver.get(url);
        LOG.i("Navigate to url=[%s]", url);
    }

    private void refresh(){
        LOG.i("refresh url '%s'", driver.getCurrentUrl());
        driver.navigate().refresh();
    }

    public void navigateAndVerify(){
        if(url.equals(driver.getCurrentUrl())){
            refresh();
        }else{
            navigate();
        }
        driver.waitForElmVisibility(navigationVerifier);
        LOG.i("navigation succeeded");
    }
}
