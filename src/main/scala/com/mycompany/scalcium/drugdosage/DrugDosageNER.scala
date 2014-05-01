package com.mycompany.scalcium.drugdosage

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

import scala.Array.canBuildFrom
import scala.collection.TraversableOnce.flattenTraversableOnce
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import com.mycompany.scalcium.utils.DictNER
import com.mycompany.scalcium.utils.ModelNER
import com.mycompany.scalcium.utils.RegexNER

class DrugDosageNER(val modelFile: File,
    val debug: Boolean = false) {

  val tmpdir = new File("/tmp")
  val modelNER = new ModelNER(modelFile)
  
  def train(drugFile: File, freqFile: File, routeFile: File,
      unitsFile: File, numPatternsFile: File,
      inputFile: File, modelFile: File,
      ngramSize: Int, numChars: Int, 
      lambda: Double): Unit = {
    // use the dict NER and regex NER to build training
    // set out of rules.
    val dictNER = new DictNER(Map(
      ("DRUG", drugFile),
      ("FREQ", freqFile),
      ("ROUTE", routeFile),
      ("UNIT", unitsFile)))
    val regexNER = new RegexNER(Map(
      ("NUM", numPatternsFile)))
    val trainWriter = new PrintWriter(
      new FileWriter(new File(tmpdir, "model.train")))
    Source.fromFile(inputFile)
      .getLines()
      .foreach(line => {
        val dicttags = dictNER.tag(line)
        val regextags = regexNER.tag(line)
        val mergedtags = dictNER.merge(List(dicttags, regextags))
        trainWriter.println(mergedtags.map(wordTag => 
          wordTag._1 + "/" + wordTag._2).mkString(" "))
    })
    trainWriter.flush()
    trainWriter.close()
    // use the bootstrapped training set to train the
    // model NER
    modelNER.train(new File(tmpdir, "model.train"), 
      modelFile, ngramSize, numChars, lambda)
  }
  
  def evaluate(nfolds: Int, ntest: Int, 
      datafile: File, ngramSize: Int, numChars: Int, 
      lambda: Double): Double = {
    val accuracies = ArrayBuffer[Double]()
    (0 until nfolds).foreach(cv => {
      // get random list of rows that will be our test case
      val testrows = scala.collection.mutable.Set[Int]()
      val random = scala.util.Random
      do {
        testrows += (random.nextDouble * 100).toInt
      } while (testrows.size < ntest)
      // partition input dataset into train and test
      // we use the model.train file from the previous test
      val evaltrain = new PrintWriter(
        new FileWriter(new File(tmpdir, "eval.train")))
      val evaltest = new PrintWriter(
        new FileWriter(new File(tmpdir, "eval.test")))
      var curr = 0
      Source.fromFile(datafile)
        .getLines()
        .foreach(line => {
        if (testrows.contains(curr)) evaltest.println(line)
        else evaltrain.println(line)
        curr += 1
      })
      evaltrain.flush()
      evaltrain.close()
      evaltest.flush()
      evaltest.close()
      // now we use evaltrain to train the model
      val modelNER = new ModelNER(null)
      modelNER.train(new File(tmpdir, "eval.train"), 
        new File(tmpdir, "eval.bin"), 
        ngramSize, numChars, lambda)
      // now test against evaltest with the model
      val trainedModelNER = new ModelNER(
        new File(tmpdir, "eval.bin"))
      val results = Source.fromFile(
        new File(tmpdir, "eval.test"))
        .getLines()
        .map(line => {
           val words = line.split(" ").map(wordTag => 
             wordTag.substring(0, wordTag.lastIndexOf('/')))
           .mkString(" ")
           val rtags = line.split(" ").map(wordTag => 
             wordTag.substring(wordTag.lastIndexOf('/') + 1))
             .toList
           val ptags = trainedModelNER.tag(words)
             .map(wordTag => wordTag._2)
           (0 until List(rtags.size, ptags.size).min)
             .map(i => if (rtags(i).equals(ptags(i))) 1 else 0)
      })
      .flatten
      .toList
      val accuracy = results.sum.toDouble / results.size
      if (debug) 
        Console.println("CV-# %d: accuracy = %f"
        .format(cv, accuracy))
      accuracies += accuracy
    })
    accuracies.sum / accuracies.size
  }
  
  def parse(s: String): List[(String,String)] = modelNER.tag(s)
}
