package ui.model.pages;

import automation.Locators;
import automation.selenium.DriverWrapper;
import org.openqa.selenium.By;
import ui.model.BasePage;
import ui.model.PageElements;

public class PhpTravlesAddUserPage extends BasePage {

    private final static By saveByBth = Locators.findBy("login_page_submit_button");
    private final static By usernameFieldByTxt = Locators.findBy("login_page_username_field");
    private final static By passwordFieldByTxt = Locators.findBy("login_page_password_field");

    public PhpTravlesAddUserPage(DriverWrapper driver){
        super(driver, "addauser.php", usernameFieldByTxt);
    }

    public void addNewUser(String username, String password){
        clearAndSetText(usernameFieldByTxt, username);
        clearAndSetText(passwordFieldByTxt, password);
        clickButton(saveByBth);
    }
}

