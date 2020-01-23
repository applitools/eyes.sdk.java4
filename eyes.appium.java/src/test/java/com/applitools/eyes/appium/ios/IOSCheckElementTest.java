package com.applitools.eyes.appium.ios;

import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Eyes;
import com.applitools.eyes.appium.Target;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class IOSCheckElementTest {

    public static void main(String[] args) throws MalformedURLException {
        Eyes eyes = new Eyes();
        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setApiKey(System.getenv("APPLITOOLS_API_KEY"));
        eyes.setSaveDebugScreenshots(false);

        DesiredCapabilities dc = new DesiredCapabilities();

        dc.setCapability(MobileCapabilityType.PLATFORM_VERSION, "12.4");
//        dc.setCapability(MobileCapabilityType.PLATFORM_VERSION, "13.0");
        dc.setCapability(MobileCapabilityType.PLATFORM_NAME, "iOS");
        dc.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
        dc.setCapability(MobileCapabilityType.DEVICE_NAME, "iPhone Xr");
//        dc.setCapability(MobileCapabilityType.DEVICE_NAME, "iPhone 11");
        dc.setCapability(MobileCapabilityType.APP, "/Users/alexanderkachechka/Downloads/FullpagescrolliOS.app");

        IOSDriver<WebElement> driver = new IOSDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), dc);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        try {
            eyes.open(driver, "iOS test application", "Check element test");

            String xpath = "//XCUIElementTypeApplication[@name=\"FullpagescrolliOS\"]/XCUIElementTypeWindow[1]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeScrollView";
            eyes.check(Target.region(By.xpath(xpath)).fully().statusBarExists());

            eyes.close();
        } finally {
            eyes.abortIfNotClosed();
            driver.quit();
        }
    }
}
