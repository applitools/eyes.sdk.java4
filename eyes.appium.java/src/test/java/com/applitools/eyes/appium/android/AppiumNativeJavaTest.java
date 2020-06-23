package com.applitools.eyes.appium.android;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import org.testng.annotations.Test;

public class AppiumNativeJavaTest extends AndroidTestSetup {

    @Test(groups = "failed") // Could not proxy. Proxy error: Request failed with status code 404
    public void testAppiumNativeJava() {
        capabilities.setCapability("browserName", "");
        capabilities.setCapability("clearSystemFiles", true);
        capabilities.setCapability("noReset", true);

        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);

        try {

            // Start the test.
            eyes.open(driver, "Contacts!", "My first Appium native Java test!");

            // Visual validation.
            eyes.checkWindow("Contact list!");

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
        capabilities.setCapability("app", "http://saucelabs.com/example_files/ContactManager.apk");
    }
}
