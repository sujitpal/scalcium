package com.mycompany.scalcium.utils

import java.io.File

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class NERTest {

  val datadir = "src/main/resources/drug_dosage"
    
  @Test
  def testDictNERTag(): Unit = {
    val dictNER = new DictNER(Map(
      ("DRUG", new File(datadir, "drugs.dict")),
      ("FREQ", new File(datadir, "frequencies.dict")),
      ("ROUTE", new File(datadir, "routes.dict")),
      ("UNIT", new File(datadir, "units.dict"))))
    val s = "hydrocortizone cream, apply to rash bid"
    val tagged = dictNER.tag(s)
    Console.println("input=" + s)
    Console.println("tagged(dictNER)=" + tagged)
    Assert.assertEquals(7, tagged.size)
    val drugs = tagged.filter(wt => "DRUG".equals(wt._2))
    Assert.assertEquals(2, drugs.size)
    val routes = tagged.filter(wt => "ROUTE".equals(wt._2))
    Assert.assertEquals(1, routes.size)
    val freqs = tagged.filter(wt => "FREQ".equals(wt._2))
    Assert.assertEquals(1, freqs.size)
  }
  
  @Test
  def testRegexNERTag(): Unit = {
    val regexNER = new RegexNER(Map(
      ("NUM", new File(datadir, "num_patterns.dict"))    
    ))
    val s = "Dyazide 37.5/25 mg po qd"
//    val s = "atenolol 50 mg tabs one qd, #100, one year"
    val tagged = regexNER.tag(s)
    Console.println("input=" + s)
    Console.println("tagged(regexNER)=" + tagged)
    Assert.assertEquals(5, tagged.size)
    val nums = tagged.filter(wt => "NUM".equals(wt._2))
    Assert.assertEquals(1, nums.size)
  }
  
  @Test
  def testTagMerge(): Unit = {
    val s = "Dyazide 37.5/25 mg po qd"
    val dictNER = new DictNER(Map(
      ("DRUG", new File(datadir, "drugs.dict")),
      ("FREQ", new File(datadir, "frequencies.dict")),
      ("ROUTE", new File(datadir, "routes.dict")),
      ("UNIT", new File(datadir, "units.dict"))))
    val regexNER = new RegexNER(Map(
      ("NUM", new File(datadir, "num_patterns.dict"))    
    ))
    val dicttags = dictNER.tag(s)
    val regextags = regexNER.tag(s)
    val mergedtags = dictNER.merge(List(dicttags, regextags))
    Console.println("input=" + s)
    Console.println("dict tags=" + dicttags)
    Console.println("regex tags=" + regextags)
    Console.println("merged tags=" + mergedtags)
    Assert.assertEquals(5, mergedtags.size)
    val untagged = mergedtags.filter(wt => "O".equals(wt._2))
    Assert.assertTrue(untagged.isEmpty)
  }

}
