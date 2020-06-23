package com.applitools.eyes.appium;

import com.applitools.eyes.ReportingTestSuite;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITest;
import org.testng.annotations.BeforeClass;

import java.net.MalformedURLException;

public abstract class TestSetup extends ReportingTestSuite implements ITest {

    protected DesiredCapabilities capabilities;
    protected AppiumDriver<MobileElement> driver;
    protected Eyes eyes;

    @Override
    public String getTestName() {
        return getClass().getName();
    }

    @BeforeClass
    public void beforeClass() {
        super.setGroupName("appium");
        capabilities = new DesiredCapabilities();
        setCapabilities();

        eyes = new Eyes();
        eyes.setApiKey(System.getenv("APPLITOOLS_API_KEY"));

        try {
            initDriver();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected abstract void initDriver() throws MalformedURLException;

    protected abstract void setCapabilities();
}
