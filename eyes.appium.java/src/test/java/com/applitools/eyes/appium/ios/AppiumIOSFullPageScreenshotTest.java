package com.applitools.eyes.appium.ios;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class AppiumIOSFullPageScreenshotTest extends IOSTestSetup {

    @Test
    public void testAppiumIOSFullPageScreenshot() {
//        eyes.setForceFullPageScreenshot(true);
//        eyes.setSaveDebugScreenshots(true);
//        eyes.setDebugScreenshotsPath("/Users/jlipps/Desktop");
        eyes.setScrollToRegion(false);
        eyes.setMatchTimeout(1000);
        eyes.setStitchOverlap(44);

        // Start the test.
        eyes.open(driver, "Applitools Demo App", "Appium Native iOS with Full page screenshot");
        WebElement showTable = driver.findElement(MobileBy.AccessibilityId("Table view"));
        eyes.checkRegion(showTable);
        showTable.click();
        eyes.checkWindow("Big Table");
//            driver.findElement(MobileBy.AccessibilityId("Collection view")).click();
//            eyes.checkWindow("Short collection view");

        // End the test.
        eyes.close();
    }

    @Override
    protected void setCapabilities() {
        super.setCapabilities();
        capabilities.setCapability("useNewWDA", false);
    }

    @Override
    protected void setResetMode() {
        capabilities.setCapability("noReset", true);
    }
}
