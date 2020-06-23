package com.applitools.eyes.appium.android;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.StdoutLogHandler;
import com.applitools.eyes.appium.Target;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class BatchSequenceNameTest extends AndroidTestSetup {

    @Test
    public void testBatchSequenceName() {
        driver.manage().timeouts().implicitlyWait(10_000, TimeUnit.MILLISECONDS);

        BatchInfo batchInfo = new BatchInfo("AndroidTestApp");
        batchInfo.setSequenceName("Test Sequence");

        eyes.setLogHandler(new StdoutLogHandler(true));
        eyes.setMatchTimeout(1000);
        eyes.setBatch(batchInfo);

        try {
            eyes.open(driver, "AndroidTestApp", "Batch Sequence Name Test");

            eyes.check(Target.window());

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
