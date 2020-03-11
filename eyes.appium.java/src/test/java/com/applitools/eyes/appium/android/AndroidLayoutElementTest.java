package com.applitools.eyes.appium.android;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class AndroidLayoutElementTest {

    public static void main(String[] args) throws MalformedURLException {
        BatchInfo batch = new BatchInfo("Test");
        batch.setSequenceName("sequence");
        Eyes eyes = new Eyes();
        eyes.setBatch(batch);
        eyes.setLogHandler(new StdoutLogHandler(true));
        //Add API Key
        eyes.setSaveDebugScreenshots(true);

        DesiredCapabilities dc = new DesiredCapabilities();

        dc.setCapability(MobileCapabilityType.PLATFORM_VERSION, "7.1.1");
        dc.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        dc.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UIautomator2");
        dc.setCapability(MobileCapabilityType.DEVICE_NAME, "Pixel3");
        dc.setCapability(MobileCapabilityType.APP, "https://applitools.bintray.com/Examples/eyes-android-hello-world.apk");

        AndroidDriver<MobileElement> driver = new AndroidDriver<MobileElement>(new URL("http://127.0.0.1:4723/wd/hub"), dc);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        try {
            // Start visual UI testing
            eyes.open(driver, "Android test application", "test");

            MobileElement elem = driver.findElementById("random_number_text_view");
            eyes.check(Target.window().layout(elem));

            // End visual UI testing. Validate visual correctness.
            eyes.close();
        } finally {
            eyes.abortIfNotClosed();
            driver.quit();
        }
    }
}
