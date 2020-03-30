package com.applitools.eyes.appium.ios;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

public class EmptyTableTest {

    public static void main(String[] args) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("deviceName", "iPhone 8");
        capabilities.setCapability("platformVersion", "12.4");
        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/IOSTestApp/1.1/IOSTestApp-1.1.zip");
        capabilities.setCapability("automationName", "XCUITest");
        capabilities.setCapability("fullReset", true);
        capabilities.setCapability("newCommandTimeout", 300);

        // Open the app.
        IOSDriver driver = new IOSDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        // Initialize the eyes SDK and set your private API key.
        Eyes eyes = new Eyes();

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
}
