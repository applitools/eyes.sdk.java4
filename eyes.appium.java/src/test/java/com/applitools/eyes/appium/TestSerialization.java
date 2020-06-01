package com.applitools.eyes.appium;

import com.applitools.eyes.ImageMatchSettings;
import com.applitools.eyes.MatchWindowTask;
import com.applitools.eyes.fluent.ICheckSettings;
import com.applitools.eyes.fluent.ICheckSettingsInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestSerialization {

    /**
     * Used for serialization testing
     */
    private static ObjectMapper jsonMapper;

    @BeforeClass
    public static void InitOnce() {
        jsonMapper = new ObjectMapper();
    }

    @DataProvider(name = "four_booleans")
    public static Object[][] fourBooleansDP() {
        return new Object[][]{
                {true, true, true, true},
                {true, true, true, false},
                {true, true, false, true},
                {true, true, false, false},
                {true, false, true, true},
                {true, false, true, false},
                {true, false, false, true},
                {true, false, false, false},
                {false, true, true, true},
                {false, true, true, false},
                {false, true, false, true},
                {false, true, false, false},
                {false, false, true, true},
                {false, false, true, false},
                {false, false, false, true},
                {false, false, false, false}
        };
    }

    @Test(dataProvider = "four_booleans")
    public void test_ImageMatchSettings_Serialization(boolean ignoreCaret, boolean useDom, boolean enablePatterns, boolean ignoreDisplacements) throws JsonProcessingException {
        ImageMatchSettings ims = new ImageMatchSettings();
        ims.setIgnoreCaret(ignoreCaret);
        ims.setUseDom(useDom);
        ims.setEnablePatterns(enablePatterns);
        ims.setIgnoreDisplacements(ignoreDisplacements);

        String actualSerialization = jsonMapper.writeValueAsString(ims);

        String expectedSerialization = String.format(
                "{\"matchLevel\":\"STRICT\",\"exact\":null,\"ignoreCaret\":%s,\"useDom\":%s,\"enablePatterns\":%s,\"ignoreDisplacements\":%s,\"accessibility\":[],\"accessibilitySettings\":null,\"Ignore\":null,\"Layout\":null,\"Strict\":null,\"Content\":null,\"Floating\":null}",
                ignoreCaret, useDom, enablePatterns, ignoreDisplacements);

        Assert.assertEquals(actualSerialization,
                expectedSerialization, "ImageMatchSettings serialization does not match!");
    }

    @Test(dataProvider = "four_booleans")
    public void test_ImageMatchSettings_Serialization_Global(boolean ignoreCaret, boolean useDom, boolean enablePatterns, boolean ignoreDisplacements) throws JsonProcessingException {
        ICheckSettings settings = Target.window().fully().useDom(useDom).enablePatterns(enablePatterns).ignoreCaret(ignoreCaret).ignoreDisplacements(ignoreDisplacements);
        TestEyes eyes = new TestEyes();
        ImageMatchSettings imageMatchSettings = MatchWindowTask.createImageMatchSettings((ICheckSettingsInternal)settings, eyes);

        String actualSerialization = jsonMapper.writeValueAsString(imageMatchSettings);

        String expectedSerialization = String.format(
                "{\"matchLevel\":\"STRICT\",\"exact\":null,\"ignoreCaret\":%s,\"useDom\":%s,\"enablePatterns\":%s,\"ignoreDisplacements\":%s,\"accessibility\":[],\"accessibilitySettings\":null,\"Ignore\":null,\"Layout\":null,\"Strict\":null,\"Content\":null,\"Floating\":null}",
                ignoreCaret, useDom, enablePatterns, ignoreDisplacements);

        Assert.assertEquals(actualSerialization,
                expectedSerialization, "ImageMatchSettings serialization does not match!");
    }
}
