package com.applitools.eyes.appium.android;

import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class AndroidScrollViewTest {

    public static void main(String[] args) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Google Nexus 6");
        capabilities.setCapability("platformVersion", "7.1.1");

        capabilities.setCapability("app", "https://applitools.bintray.com/Examples/android/1.2/app_android.apk");
        capabilities.setCapability("automationName", "UiAutomator2");
        capabilities.setCapability("newCommandTimeout", 300);

        AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        driver.manage().timeouts().implicitlyWait(10_000, TimeUnit.MILLISECONDS);

        Eyes eyes = new Eyes();

        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setMatchTimeout(1000);

        try {
            eyes.open(driver, "AndroidTestApp", "Check ScrollView");

            // Scroll down
            TouchAction scrollAction = new TouchAction(driver);
            scrollAction.press(new PointOption().withCoordinates(5, 1700)).waitAction(new WaitOptions().withDuration(Duration.ofMillis(1500)));
            scrollAction.moveTo(new PointOption().withCoordinates(5, 100));
            scrollAction.cancel();
            driver.performTouchAction(scrollAction);

            driver.findElementById("btn_scroll_view_footer_header").click();

            eyes.check(Target.window().fully().withName("Fullpage"));

            eyes.close();
        } finally {
            driver.quit();
            eyes.abortIfNotClosed();
        }
    }
}
