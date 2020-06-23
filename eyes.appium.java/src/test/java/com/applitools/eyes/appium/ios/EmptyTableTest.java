package com.applitools.eyes.appium.ios;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileBy;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class EmptyTableTest extends IOSTestSetup {

    @Test
    public void testEmptyTable() {
        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);
        eyes.setMatchTimeout(1000);

        try {
            // Start the test.
            eyes.open(driver, "IOS test application", "Empty XCUIElementTypeTable");

            WebElement showTable = driver.findElement(MobileBy.AccessibilityId("Empty table view"));
            showTable.click();

            eyes.check(Target.window().fully().withName("Fullpage"));

            // End the test.
            eyes.close();

        } finally {

            // Close the app.
            driver.quit();

            // If the test was aborted before eyes.close was called, ends the test as aborted.
            eyes.abortIfNotClosed();

        }

    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/IOSTestApp/1.1/IOSTestApp-1.1.zip");
    }
}
