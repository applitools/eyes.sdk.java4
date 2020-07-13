package com.applitools.eyes.appium.android;

import com.applitools.eyes.LogHandler;
import com.applitools.eyes.StdoutLogHandler;
import org.testng.annotations.Test;

// TODO: change to unit test
public class DeviceNameTest extends AndroidTestSetup {

    @Test
    public void testDeviceName() {
        eyes.setScrollToRegion(false);
        eyes.open(driver, "Applitools Android Test App", "Test device name");
        eyes.checkWindow();
        eyes.close();
    }
}
