package com.applitools.eyes.appium;

import com.applitools.eyes.EyesBase;
import com.applitools.eyes.EyesScreenshot;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.config.Configuration;

public class TestEyes extends EyesBase {

    private Configuration configuration = new Configuration();

    public TestEyes() {
    }

    @Override
    protected String getBaseAgentId() {
        return null;
    }

    @Override
    public String tryCaptureDom() {
        return null;
    }

    @Override
    protected RectangleSize getViewportSize() {
        return new RectangleSize(100, 100);
    }

    @Override
    protected void setViewportSize(RectangleSize size) {
    }

    @Override
    protected String getInferredEnvironment() {
        return "TestEyes";
    }

    @Override
    protected EyesScreenshot getScreenshot() {
        return new TestEyesScreenshot(this.logger, null);
    }

    @Override
    protected String getTitle() {
        return "TestEyes_Title";
    }

    @Override
    protected String getAUTSessionId() {
        return null;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected Configuration getConfiguration() {
        return configuration;
    }
}
