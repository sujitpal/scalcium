package com.mycompany.scalcium.utils

import org.junit.Test
import org.junit.Assert
import org.junit.Test

class ConfigUtilsTest {

  @Test def testGetNonDefaultBooleanValue(): Unit = {
    val x = ConfigUtils.getBooleanValue("relayhealth", "useHealthstatExclusion", false)
    Console.println("relayhealth.useHealthstatExclusion=" + x)
    Assert.assertTrue(x)
  }

  @Test def testGetDefaultBooleanValue(): Unit = {
    val x = ConfigUtils.getDefaultBooleanValue("handleNegation", false)
    Console.println("default.handleNegation=" + x)
    Assert.assertFalse(x)
  }
  
  @Test def testGetUndefinedNonDefaultBooleanValue(): Unit = {
    val x = ConfigUtils.getBooleanValue("relayhealth", "excludedTermRemover", false)
    Console.println("relayhealth.excludedTermRemover=" + x)
    Assert.assertTrue(x)
  }
  
  @Test def testGetUndefinedDefaultBooleanValue(): Unit = {
    val x = ConfigUtils.getBooleanValue("foo", "bar", false)
    Console.println("foo.bar=" + x)
    Assert.assertFalse(x)
  }
  
  @Test def testGetStringValue(): Unit = {
    val x = ConfigUtils.getStringValue("els", "bodyBaseOnlyScoreCutoffMode", "undefined");
    Console.println("els.bodyBaseOnlyScoreCutoffMode=" + x)
    Assert.assertEquals("off", x)
  }
  
  @Test def testGetDefaultStringValue(): Unit = {
    val x = ConfigUtils.getDefaultStringValue("bodyBaseOnlyScoreCutoffMode", "undefined")
    Console.println("default.bodyBaseOnlyScoreCutoffMode=" + x)
    Assert.assertEquals("only", x)
  }
  
  @Test def testGetIntValue(): Unit = {
    val x = ConfigUtils.getIntValue("jj", "ancestorMaxGenerations", 1)
    Console.println("jj.ancestorMaxGenerations=" + x)
    Assert.assertEquals(3, x)
  }
  
  @Test def testGetDefaultIntValue(): Unit = {
    val x = ConfigUtils.getIntValue("default", "ancestorMaxGenerations", 1)
    Console.println("default.ancestorMaxGenerations=" + x)
    Assert.assertEquals(3, x)
  }
  
  @Test def testGetFloatValue(): Unit = {
    val x = ConfigUtils.getFloatValue("jj", "baseScoreCutoffValue", 0.0F)
    Console.println("jj.baseScoreCutoffValue=" + x)
    Assert.assertEquals(0.5F, x, 0.0001F)
  }
  
  @Test def testGetDefaultFloatValue(): Unit = {
    val x = ConfigUtils.getDefaultFloatValue("baseScoreCutoffValue", -1.0F)
    Console.println("jj.baseScoreCutoffValue=" + x)
    Assert.assertEquals(0.0F, x, 0.0001F)
  }
}
