package com.applitools.eyes.appium;

import com.applitools.eyes.Region;
import com.applitools.eyes.appium.fluent.IgnoreRegionByElement;
import com.applitools.eyes.selenium.fluent.FrameLocator;
import com.applitools.eyes.selenium.fluent.SeleniumCheckSettings;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;

public class EyesAppiumCheckSettings extends SeleniumCheckSettings {

    private By cutElementSelector;
    private ElementType cutElementType;

    protected EyesAppiumCheckSettings() {
        super();
    }

    protected EyesAppiumCheckSettings(Region region) {
        super(region);
    }

    protected EyesAppiumCheckSettings(By targetSelector) {
        super(targetSelector);
    }

    protected EyesAppiumCheckSettings(WebElement targetElement) {
        super(targetElement);
    }

    @Override
    public EyesAppiumCheckSettings clone() {
        EyesAppiumCheckSettings clone = new EyesAppiumCheckSettings();
        super.populateClone(clone);
        clone.targetElement = this.targetElement;
        clone.targetSelector = this.targetSelector;
        clone.frameChain.addAll(this.frameChain);
        clone.cutElementSelector = this.cutElementSelector;
        clone.cutElementType = this.cutElementType;
        return clone;
    }

    @Override
    public EyesAppiumCheckSettings frame(By by) {
        EyesAppiumCheckSettings clone = this.clone();
        FrameLocator fl = new FrameLocator();
        fl.setFrameSelector(by);
        clone.frameChain.add(fl);
        return clone;
    }

    @Override
    public EyesAppiumCheckSettings frame(String frameNameOrId) {
        EyesAppiumCheckSettings clone = this.clone();
        FrameLocator fl = new FrameLocator();
        fl.setFrameNameOrId(frameNameOrId);
        clone.frameChain.add(fl);
        return clone;
    }

    @Override
    public EyesAppiumCheckSettings frame(int index) {
        EyesAppiumCheckSettings clone = this.clone();
        FrameLocator fl = new FrameLocator();
        fl.setFrameIndex(index);
        clone.frameChain.add(fl);
        return clone;
    }

    @Override
    public EyesAppiumCheckSettings frame(WebElement frameReference) {
        EyesAppiumCheckSettings clone = this.clone();
        FrameLocator fl = new FrameLocator();
        fl.setFrameReference(frameReference);
        clone.frameChain.add(fl);
        return clone;
    }

    @Override
    public EyesAppiumCheckSettings layout(WebElement[] elements) {
        EyesAppiumCheckSettings clone = this.clone();
        for (WebElement e : elements) {
            clone.layout_(new IgnoreRegionByElement(e));
        }
        return clone;
    }

    public EyesAppiumCheckSettings layout(WebElement element, WebElement... elements) {
        EyesAppiumCheckSettings clone = this.clone();
        clone.layout_(new IgnoreRegionByElement(element));
        for (WebElement e : elements) {
            clone.layout_(new IgnoreRegionByElement(e));
        }
        return clone;
    }

    public EyesAppiumCheckSettings ignore(WebElement element, WebElement... elements) {
        EyesAppiumCheckSettings clone = this.clone();
        clone.ignore_(new IgnoreRegionByElement(element));
        for (WebElement e : elements) {
            clone.ignore_(new IgnoreRegionByElement(e));
        }
        return clone;
    }

    public EyesAppiumCheckSettings ignore(WebElement[] elements) {
        EyesAppiumCheckSettings clone = this.clone();
        for (WebElement e : elements) {
            clone.ignore_(new IgnoreRegionByElement(e));
        }
        return clone;
    }

    public EyesAppiumCheckSettings strict(WebElement element, WebElement... elements) {
        EyesAppiumCheckSettings clone = this.clone();
        clone.strict_(new IgnoreRegionByElement(element));
        for (WebElement e : elements) {
            clone.strict_(new IgnoreRegionByElement(e));
        }
        return clone;
    }

    public EyesAppiumCheckSettings strict(WebElement[] elements) {
        EyesAppiumCheckSettings clone = this.clone();
        for (WebElement e : elements) {
            clone.strict_(new IgnoreRegionByElement(e));
        }
        return clone;
    }

    public EyesAppiumCheckSettings content(WebElement element, WebElement... elements) {
        EyesAppiumCheckSettings clone = this.clone();
        clone.content_(new IgnoreRegionByElement(element));
        for (WebElement e : elements) {
            clone.content_(new IgnoreRegionByElement(e));
        }
        return clone;
    }

    public EyesAppiumCheckSettings content(WebElement[] elements) {
        EyesAppiumCheckSettings clone = this.clone();
        for (WebElement e : elements) {
            clone.content_(new IgnoreRegionByElement(e));
        }
        return clone;
    }

    public EyesAppiumCheckSettings cut(@Nonnull ElementType type, @Nonnull By selector) {
        EyesAppiumCheckSettings clone = this.clone();
        clone.cutElementSelector = selector;
        clone.cutElementType = type;
        return clone;
    }

    public ElementType getCutElementType() {
        return cutElementType;
    }

    public By getCutElementSelector() {
        return cutElementSelector;
    }
}
