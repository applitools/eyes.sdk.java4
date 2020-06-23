package com.applitools.eyes.appium.android;

import com.applitools.eyes.AccessibilityRegionType;
import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileElement;
import io.appium.java_client.remote.MobileCapabilityType;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class AndroidLayoutElementTest extends AndroidTestSetup {

    @Test
    public void testAndroidLayoutElement() {
        BatchInfo batch = new BatchInfo("Regions test");
        Eyes eyes = new Eyes();
        eyes.setBatch(batch);
        eyes.setLogHandler(new StdoutLogHandler(true));
        //Add API Key
        eyes.setSaveDebugScreenshots(true);

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        try {
            // Start visual UI testing
            eyes.open(driver, "Android test application", "Test regions with element");

            MobileElement elem = driver.findElementById("random_number_text_view");
            eyes.check(Target.window().layout(elem).withName("layout()"));

            eyes.check(Target.window().content(elem).withName("content()"));

            eyes.check(Target.window().strict(elem).withName("strict()"));

            eyes.check(Target.window().ignore(elem).withName("ignore()"));

            eyes.check(Target.window().floating(elem, 100, 100, 100, 100).withName("floating()"));

            eyes.check(Target.window().accessibility(elem, AccessibilityRegionType.RegularText).withName("accessibility()"));

            // End visual UI testing. Validate visual correctness.
            eyes.close();
        } finally {
            eyes.abortIfNotClosed();
            driver.quit();
        }
    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability(MobileCapabilityType.APP, "https://applitools.bintray.com/Examples/eyes-android-hello-world.apk");
    }
}
