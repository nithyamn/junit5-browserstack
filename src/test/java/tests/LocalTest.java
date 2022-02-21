package tests;
import org.openqa.selenium.remote.DesiredCapabilities;
import runners.BstackRunner;
import utils.MarkSessionStatus;
import utils.SetupLocalTesting;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import runners.WebDriverTest;


@ExtendWith({SetupLocalTesting.class})
public class LocalTest {
    @WebDriverTest
    void localTest(DesiredCapabilities capabilities) throws ParseException {
        MarkSessionStatus sessionStatus = new MarkSessionStatus();
        BstackRunner runner = new BstackRunner();
        WebDriver driver = runner.setupWebDriver(capabilities);
        try{
            driver.get("http://localhost:45691/check");
            String validateContent = driver.findElement(By.cssSelector("body")).getText();
            if(validateContent.contains("Up and running"))
                sessionStatus.markTestStatus("passed", "Local content validated!",driver);
            else
                sessionStatus.markTestStatus("failed", "Local content not validated!",driver);
            driver.quit();
        }catch (Exception e){
            sessionStatus.markTestStatus("failed", "There was some issue!",driver);
            driver.quit();
            System.out.println(e.getMessage());
        }
    }
}