package com.applitools.eyes.appium.android;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import org.testng.annotations.Test;

public class AppiumNativeJavaTest extends AndroidTestSetup {

    @Test
    public void testAppiumNativeJava() {
        // Start the test.
        eyes.open(driver, "Android Test App", "My first Appium native Java test!");

        // Visual validation.
        eyes.checkWindow("Launch screen test");

        // End the test.
        eyes.close();
    }

    @Override
    public void setCapabilities() {
        super.setCapabilities();
        capabilities.setCapability("browserName", "");
        capabilities.setCapability("clearSystemFiles", true);
        capabilities.setCapability("noReset", true);
    }
}
