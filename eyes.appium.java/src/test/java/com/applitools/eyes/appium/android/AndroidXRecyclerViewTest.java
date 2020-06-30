package com.applitools.eyes.appium.android;

import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileBy;
import org.testng.annotations.Test;

public class AndroidXRecyclerViewTest extends AndroidTestSetup {

    @Test(groups = "failed") // Coordinate [x=1.0, y=-21.0] is outside of element rect: [0,0][1080,1794]
    public void testAndroidXRecyclerView() {
        eyes.setMatchTimeout(1000);

        driver.findElementById("btn_recycler_view_activity").click();

        eyes.open(driver, "AndroidXTestApp", "Test RecyclerView");

        eyes.check(Target.window().withName("Viewport"));

        eyes.check(Target.window().fully().withName("Fullpage"));

        eyes.check(Target.region(MobileBy.id("recycler_view")).withName("Region viewport"));

        eyes.check(Target.region(MobileBy.id("recycler_view")).fully().withName("Region fullpage"));

        eyes.close();
    }

    @Override
    protected void setAppCapability() {
        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/androidx/1.0.0/app_androidx.apk");
    }
}
