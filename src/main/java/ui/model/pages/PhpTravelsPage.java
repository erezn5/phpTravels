package ui.model.pages;

import automation.Locators;
import automation.selenium.DriverWrapper;
import org.awaitility.Duration;
import org.openqa.selenium.By;
import ui.model.BasePage;

public class PhpTravelsPage extends BasePage {
    private final static By usernameFieldByTxt = Locators.findBy("login_page_username_field");
    private final static By passwordFieldByTxt = Locators.findBy("login_page_password_field");
    private final static By submitBth = Locators.findBy("login_page_submit_button");

    public PhpTravelsPage(DriverWrapper driver){
        super(driver, "login.php", submitBth);
    }

    public boolean login(String username, String password) {
        clearAndSetText(usernameFieldByTxt, username);
        clearAndSetText(passwordFieldByTxt, password);
        clickButton(submitBth);
        return isSuccesful();
    }

    public boolean isSuccesful(){
        By successfulMessageTxtBy = By.xpath("//b[contains(text(), '**Successful Login**')]");
        return waitForTextElmVisibility(successfulMessageTxtBy, "**Successful Login**", Duration.FIVE_SECONDS);
    }
}
