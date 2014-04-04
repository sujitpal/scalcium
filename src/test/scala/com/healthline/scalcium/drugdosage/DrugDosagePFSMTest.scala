package com.healthline.scalcium.drugdosage

import org.junit.Test
import org.junit.Ignore
import com.healthline.scalcium.utils.DictNER
import java.io.File
import com.healthline.scalcium.utils.RegexNER
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import com.healthline.scalcium.utils.NGram
import java.io.PrintWriter
import java.io.FileWriter

class DrugDosagePFSMTest {

  val datadir = "/home/sujit/Projects/med_data/drug_dosage"

  @Test
  @Ignore
  def testComputeTransitionFrequencies(): Unit = {
    val dictNER = new DictNER(Map(
      ("DRUG", new File(datadir, "drugs.dict")),
      ("FREQ", new File(datadir, "frequencies.dict")),
      ("ROUTE", new File(datadir, "routes.dict")),
      ("UNIT", new File(datadir, "units.dict"))))
    val regexNER = new RegexNER(Map(
      ("NUM", new File(datadir, "num_patterns.dict"))    
    ))
    val transitions = ArrayBuffer[(String,String)]()
    Source.fromFile(new File(datadir, "input.txt"))
      .getLines()
      .foreach(line => {
        val dictTags = dictNER.tag(line)
        val regexTags = regexNER.tag(line)
        val mergedTags = dictNER.merge(List(dictTags, regexTags))
        val tagBigrams = NGram.bigrams(mergedTags.map(_._2))
          .map(bigram => transitions += 
            ((bigram(0).asInstanceOf[String], 
             bigram(1).asInstanceOf[String])))
    })
    val transitionFreqs = transitions
      .groupBy(pair => pair._1 + " -> " + pair._2)
      .map(pair => (pair._1, pair._2.size))
      .toList
      .sortBy(pair => pair._1)
    Console.println(transitionFreqs.mkString("\n"))
  }  

  @Test
  def testParse(): Unit = {
    val ddPFSM = new DrugDosagePFSM(
        new File(datadir, "drugs.dict"),
        new File(datadir, "frequencies.dict"),
        new File(datadir, "routes.dict"),
        new File(datadir, "units.dict"),
        new File(datadir, "num_patterns.dict"),
        false)
    val writer = new PrintWriter(new FileWriter(
      new File(datadir, "pfsm_output.txt")))
    Source.fromFile(new File(datadir, "input.txt"))
      .getLines()
      .foreach(line => {
         val stab = ddPFSM.parse(line)
         writer.println(line)
         writer.println(stab.map(st => st._2 + "/" + st._1)
           .mkString(" "))
         writer.println()
    })
    writer.flush()
    writer.close()
  }

}