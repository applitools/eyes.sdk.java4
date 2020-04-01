package com.applitools.eyes.appium.android;

import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;

public class AndroidXRecyclerViewTest {

    public static void main(String[] args) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Pixel 2");
        capabilities.setCapability("platformVersion", "9.0");

        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/androidx/1.0.0/app_androidx.apk");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 300);

        AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        Eyes eyes = new Eyes();

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
}
