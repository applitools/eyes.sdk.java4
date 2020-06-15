package com.applitools.eyes.selenium;

import com.applitools.connectivity.ServerConnector;
import com.applitools.eyes.EyesBase;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.selenium.fluent.Target;
import org.testng.annotations.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class TestServerConnector {

    @Test
    public void TestDelete() {
        Eyes eyes = new Eyes();
        eyes.setServerConnector(new ServerConnector(eyes.getLogger(), EyesBase.getDefaultServerUrl()));
        WebDriver webDriver = new ChromeDriver();
        try {
            WebDriver driver = eyes.open(webDriver,
                    TestServerConnector.class.getSimpleName(),
                    TestServerConnector.class.getSimpleName(), new RectangleSize(800, 599));

            driver.get("https://applitools.com/helloworld");

            eyes.check("Hello", Target.window());

            TestResults results = eyes.close();

            results.delete();
        }
        finally {
            webDriver.quit();
            eyes.abortIfNotClosed();
        }
    }

}
