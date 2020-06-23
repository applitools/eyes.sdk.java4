package com.applitools.eyes.appium.android;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import org.testng.annotations.Test;

public class DeviceNameTest extends AndroidTestSetup {

    @Test
    public void testDeviceName() {
        LogHandler logHandler = new StdoutLogHandler(true);
        eyes.setLogHandler(logHandler);
        eyes.setScrollToRegion(false);

        try {
            eyes.open(driver, "Applitools Android Test App", "Test device name");
            eyes.checkWindow();
            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/app-android.apk");
    }
}
