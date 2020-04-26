package com.applitools.eyes.selenium;

import com.applitools.eyes.*;
import com.applitools.eyes.metadata.ActualAppOutput;
import com.applitools.eyes.metadata.SessionResults;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.selenium.wrappers.EyesWebDriver;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestSendDom {

    private interface WebDriverInitializer {
        void initWebDriver(WebDriver webDriver);
    }

    private static void captureDom(String url, String testName) {
        captureDom(url, null, testName);
    }

    private static void captureDom(String url, WebDriverInitializer initCode, String testName) {
        WebDriver webDriver = createChromeDriver();
        webDriver.get(url);
        Logger logger = new Logger();

        logger.setLogHandler(new StdoutLogHandler(true));
        if (initCode != null) {
            initCode.initWebDriver(webDriver);
        }
        Eyes eyes = new Eyes();
        try {
            eyes.setBatch(createBatch());
            EyesWebDriver eyesWebDriver = (EyesWebDriver) eyes.open(webDriver, "Test Send DOM", testName);
            //DomCapture domCapture = new DomCapture(logger, eyesWebDriver);
            eyes.checkWindow();
            TestResults results = eyes.close(false);
            boolean hasDom = getHasDom(eyes, results);
            Assert.assertTrue(hasDom);
            //string actualDomJsonString = domCapture.GetFullWindowDom();
            //WriteDomJson(logger, actualDomJsonString);

        } catch (Exception ex) {
            GeneralUtils.logExceptionStackTrace(eyes.getLogger(), ex);
            throw ex;
        } finally {
            eyes.abortIfNotClosed();
            webDriver.quit();
        }
    }


    class DomInterceptingEyes extends Eyes {
        private String domJson;

        public DomInterceptingEyes() {
        }

        public String getDomJson() {
            return domJson;
        }

        @Override
        public String tryCaptureDom() {
            this.domJson = super.tryCaptureDom();
            return this.domJson;
        }
    }

    @Test
    public void TestSendDOM_FullWindow() {
        WebDriver webDriver = createChromeDriver();
        webDriver.get("https://applitools.github.io/demo/TestPages/FramesTestPage/");
        DomInterceptingEyes eyes = new DomInterceptingEyes();
        eyes.setBatch(createBatch());
        EyesWebDriver eyesWebDriver = (EyesWebDriver) eyes.open(webDriver, "Test Send DOM", "Full Window");
        try {
            eyes.check("Window", Target.window().fully());
            String actualDomJsonString = eyes.getDomJson();

            TestResults results = eyes.close(false);
            boolean hasDom = getHasDom(eyes, results);
            Assert.assertTrue(hasDom);
            ObjectMapper mapper = new ObjectMapper();
            try {
                String expectedDomJson = GeneralUtils.readToEnd(TestSendDom.class.getResourceAsStream("/expected_dom1.json"));
                JsonNode actual = mapper.readTree(actualDomJsonString);
                JsonNode expected = mapper.readTree(expectedDomJson);
                //noinspection SimplifiedTestNGAssertion
                Assert.assertTrue(actual.equals(expected));

                SessionResults sessionResults = TestUtils.getSessionResults(eyes.getApiKey(), results);
                ActualAppOutput[] actualAppOutput = sessionResults.getActualAppOutput();
                String downloadedDomJsonString = TestUtils.getStepDom(eyes, actualAppOutput[0]);
                JsonNode downloaded = mapper.readTree(downloadedDomJsonString);
                //noinspection SimplifiedTestNGAssertion
                Assert.assertTrue(downloaded.equals(expected));

            } catch (IOException e) {
                GeneralUtils.logExceptionStackTrace(eyes.getLogger(), e);
            }
        } finally {
            eyes.abortIfNotClosed();
            webDriver.quit();
        }
    }

    @Test
    public void TestSendDOM_Selector() {
        WebDriver webDriver = createChromeDriver();
        webDriver.get("https://applitools.github.io/demo/TestPages/DomTest/dom_capture.html");
        Eyes eyes = new Eyes();
        eyes.setBatch(createBatch());
        try {
            eyes.open(webDriver, "Test SendDom", "Test SendDom", new RectangleSize(1000, 700));
            eyes.check("region", Target.region(By.cssSelector("#scroll1")));
            TestResults results = eyes.close(false);
            boolean hasDom = getHasDom(eyes, results);
            Assert.assertTrue(hasDom);
        } finally {
            eyes.abortIfNotClosed();
            webDriver.quit();
        }
    }

    @Test
    public void TestNotSendDOM() {
        WebDriver webDriver = createChromeDriver();
        webDriver.get("https://applitools.com/helloworld");
        Eyes eyes = new Eyes();
        eyes.setBatch(createBatch());
        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setSendDom(false);
        try {
            eyes.open(webDriver, "Test NOT SendDom", "Test NOT SendDom", new RectangleSize(1000, 700));
            eyes.check("window", Target.window().sendDom(false));
            TestResults results = eyes.close(false);
            boolean hasDom = getHasDom(eyes, results);
            Assert.assertFalse(hasDom);
        } finally {
            eyes.abortIfNotClosed();
            webDriver.quit();
        }
    }

    @Test
    public void TestSendDOM_1() {
        captureDom("https://applitools.github.io/demo/TestPages/DomTest/dom_capture.html", "TestSendDOM_1");
    }

    @Test
    public void TestSendDOM_2() {
        captureDom("https://applitools.github.io/demo/TestPages/DomTest/dom_capture_2.html", "TestSendDOM_2");
    }

    private static boolean getHasDom(Eyes eyes, TestResults results) {
        SessionResults sessionResults = null;
        try {
            sessionResults = TestUtils.getSessionResults(eyes.getApiKey(), results);
        } catch (IOException e) {
            GeneralUtils.logExceptionStackTrace(eyes.getLogger(), e);
        }
        ActualAppOutput[] actualAppOutputs = sessionResults.getActualAppOutput();
        Assert.assertEquals(actualAppOutputs.length, 1);
        boolean hasDom = actualAppOutputs[0].getImage().getHasDom();
        return hasDom;
    }

    private static WebDriver createChromeDriver() {
        return new ChromeDriver();
    }

    private static BatchInfo createBatch() {
        return new BatchInfo("Java 4 Test");
    }
}
