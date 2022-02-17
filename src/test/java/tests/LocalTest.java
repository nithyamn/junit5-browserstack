package tests;
import runners.BstackRunner;
import runners.MarkSessionStatus;
import runners.InitCredsAndLocal;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;


@ExtendWith({BstackRunner.class, InitCredsAndLocal.class})
public class LocalTest {
    @TestTemplate
    void localTest(WebDriver driver) throws ParseException {
        MarkSessionStatus sessionStatus = new MarkSessionStatus();

        driver.get("http://localhost:45691/check");
        String validateContent = driver.findElement(By.cssSelector("body")).getText();
        if(validateContent.contains("Up and running"))
            sessionStatus.markTestStatus("passed", "Local content validated!",driver);
        else
            sessionStatus.markTestStatus("failed", "Local content not validated!",driver);
        driver.quit();
    }
}