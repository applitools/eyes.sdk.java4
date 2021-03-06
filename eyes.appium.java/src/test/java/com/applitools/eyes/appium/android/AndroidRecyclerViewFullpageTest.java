package com.applitools.eyes.appium.android;

import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Target;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class AndroidRecyclerViewFullpageTest extends AndroidTestSetup {

    @Test
    public void testAndroidRecyclerViewFullpage() {
        driver.manage().timeouts().implicitlyWait(10_000, TimeUnit.MILLISECONDS);

        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setMatchTimeout(1000);

        try {
            driver.findElementById("btn_recycler_view").click();

            eyes.open(driver, "AndroidTestApp", "Test RecyclerView");

            eyes.check(Target.window().withName("Viewport"));

            eyes.check(Target.window().fully().withName("Fullpage"));

            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/android_test_app.apk");
    }
}
