import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ui.model.pages.PhpTravelsPage;
import ui.model.pages.PhpTravlesAddUserPage;

public class ExampleTest extends BaseUITest {

    PhpTravelsPage phpTravelsPage;
    PhpTravlesAddUserPage phpTravlesAddUserPage;

    @BeforeClass
    public void setup() {
        phpTravelsPage = new PhpTravelsPage(driverWrapper);
        phpTravlesAddUserPage = new PhpTravlesAddUserPage(driverWrapper);
    }

    @Test(priority = 10)
    public void navigateToAddUserPage(){
        phpTravlesAddUserPage.navigateAndVerify();
    }

    @Test(priority = 15)
    public void addUser(){
        phpTravlesAddUserPage.addNewUser("test", "test");
    }

    @Test(priority = 20)
    public void navigateToPhpTravel(){
        phpTravelsPage.navigateAndVerify();
    }

    @Test(priority = 25)
    public void login(){
        Assert.assertTrue(phpTravelsPage.login("test", "test"));
    }
}
