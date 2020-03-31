package ui.model;

import automation.conf.EnvConf;
import automation.selenium.DriverWrapper;
import automation.utils.Waiter;
import org.awaitility.Duration;
import org.awaitility.core.Condition;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeUnit;

import static automation.logger.LoggerFactory.LOG;

public class PageElements {
    protected final DriverWrapper driver;
    private final static Duration WAIT_TIMEOUT = new Duration(EnvConf.getAsInteger("ui.locator.timeout.sec"), TimeUnit.SECONDS);
    protected PageElements(DriverWrapper driver) {
        this.driver = driver;
    }

    protected boolean waitForTextElmVisibility(By by, String text, Duration duration){
        Condition<Boolean> condition = () -> {
            try{
                return waitForElmContainsText(by, text);
            }catch (StaleElementReferenceException e){
                LOG.d(e, "failed to locate label " + text);
                return false;
            }
        };
        return Waiter.waitCondition(duration, condition);
    }

    protected boolean waitForTextElmVisibilityByElem(WebElement element, String text){
        Condition<Boolean> condition = () -> {
            try{
                return waitForElmContainsText(element, text);
            }catch (StaleElementReferenceException e){
                LOG.d(e, "failed to locate label " + text);
                return false;
            }
        };
        return Waiter.waitCondition(WAIT_TIMEOUT, condition);
    }

    protected boolean waitForElmContainsText(By by , String text){
        return waitForElmContainsText(WAIT_TIMEOUT, by, text);
    }

    private void printElmTextNotFound(By elmBy , String content){
        LOG.i("locator=[%s] NOT contains text=[%s]" , elmBy , content);
    }

    protected boolean waitForElmContainsText(Duration duration, By by , String text){
        boolean contains = driver.waitForElmContains(duration, by , text);
        if(contains){
            printElmText(by , text);
        }else{
            printElmTextNotFound(by , text);
        }
        return contains;
    }
    private void printElmText(By elmBy , String content){
        LOG.i("locator=[%s] contains text=[%s]" , elmBy , content);
    }

    private void printElmTextNotFound(WebElement element , String content){
        LOG.i("element=[%s] NOT contains text=[%s]" , element , content);
    }

    private boolean waitForElmContainsText(WebElement element , String text){
        boolean contains = driver.waitForElmContains(element , text);
        if(contains){
            printSet(element , text);
        }else{
            printElmTextNotFound(element , text);
        }
        return contains;
    }

    protected void clickButton(By byBth){
        clickButton(byBth, Duration.TEN_SECONDS);
    }

    protected void clickButton(By byBth, Duration timeout){
        WebElement bthElem = waitForClickableElm(byBth, timeout);
        clickButton(bthElem);
    }
    protected void clickButton(WebElement bthElm){
        bthElm.click();
//        printClick(bthElm);
    }

    private void printClick(WebElement elementBth){
        LOG.i("click on '%s' button" , elementBth);
    }

    private WebElement waitForClickableElm(By by, Duration timeout){
        WebElement element = driver.waitForElmClickable(timeout, by);
//        printClickableElm(by);
        return element;
    }

    private void printClickableElm(By by){
        LOG.i("locator=[%s] is clickable" , by.toString());
    }

    protected void clearAndSetText(By inputBy , String text){
        WebElement inputElm = driver.waitForElmClickable(inputBy);
        clearAndSetText(inputElm, text);
    }

    private void clearAndSetText(WebElement inputElm , String text){
        inputElm.click();
        sleep(Duration.TWO_HUNDRED_MILLISECONDS);
        inputElm.clear();
        sleep(Duration.TWO_HUNDRED_MILLISECONDS);
        inputElm.sendKeys(text);
        printSet(inputElm , text);
    }

    // NOTICE: don't use it only if you must to!
    protected static void sleep(Duration duration){
        try {
            Thread.sleep(duration.getTimeUnit().toMillis(duration.getValue()));
        } catch (InterruptedException e) {
            LOG.e(e , "error occur while sleep, timeout=[%s]" , duration.toString());
            throw new RuntimeException(e);
        }
    }
    private void printSet(WebElement txtElm , String txt){
//        LOG.i("set '%s' with value '%s'" , txtElm , txt);
    }
    protected void setText(By byTxt , String text){
        waitForClickableElm(byTxt).sendKeys(text);
        printSet(byTxt , text);
    }
    private void printSet(By txtBy , String txt){
        LOG.i("set '%s' with value '%s'" , txtBy.toString() , txt);
    }
    protected WebElement waitForClickableElm(By by){
        return waitForClickableElm(by, WAIT_TIMEOUT);
    }
}
