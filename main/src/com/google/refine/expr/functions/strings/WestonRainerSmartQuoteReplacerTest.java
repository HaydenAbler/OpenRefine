/*package com.google.refine.expr.functions.strings;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.refine.tests.util.TestUtils;


public class WestonRainerSmartQuoteReplacerTest {
    SmartQuoteReplacer STU;
    
    @Before
    public void setup()
    {
        STU = new SmartQuoteReplacer();
    }
    
    @Test
    public void serializeSmartQuoteReplacerTest() {
        String json = "{\"description\":\"Returns s that has all smart quotes replace with \\\"\",\"params\":\"string s\",\"returns\":\"string\"}";
        TestUtils.isSerializedTo(new SmartQuoteReplacer(), json);
    }
    
    @Test
    public void whenReplacingSmartQuote_givenAStringWIthASmartQuote_thenReturnStringWithNormalQUote()
    {
        String testString = "“Smart Quotes Suck”";
        String stringHolder[] = new String[1];
        stringHolder[0] = testString;
        Assert.assertEquals("\"Smart Quotes Suck\"", STU.call(null, stringHolder).toString());
    }
}
*/