package com.applitools.eyes.appium.android;

import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileBy;
import org.testng.annotations.Test;

public class AndroidXRecyclerViewTest extends AndroidTestSetup {

    @Test(groups = "failed") // Error executing adbExec
    public void testAndroidXRecyclerView() {
        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setMatchTimeout(1000);

        try {
            driver.findElementById("btn_recycler_view_activity").click();

            eyes.open(driver, "AndroidXTestApp", "Test RecyclerView");

            eyes.check(Target.window().withName("Viewport"));

            eyes.check(Target.window().fully().withName("Fullpage"));

            eyes.check(Target.region(MobileBy.id("recycler_view")).withName("Region viewport"));

            eyes.check(Target.region(MobileBy.id("recycler_view")).fully().withName("Region fullpage"));

            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/androidx/1.0.0/app_androidx.apk");
    }
}
