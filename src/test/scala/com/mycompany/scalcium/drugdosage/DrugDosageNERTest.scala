package com.mycompany.scalcium.drugdosage

import java.io.File

import scala.io.Source

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class DrugDosageNERTest {

  val datadir = "src/main/resources/drug_dosage"

  @Test
  @Ignore
  def testTrainTest(): Unit = {
    val ddNER = new DrugDosageNER(null, true)
    val inputfile = new File(datadir, "input.txt")
    val modelfile = new File(datadir, "model.bin")
    ddNER.train(
      new File(datadir, "drugs.dict"),
      new File(datadir, "frequencies.dict"),
      new File(datadir, "routes.dict"),
      new File(datadir, "units.dict"),
      new File(datadir, "num_patterns.dict"),
      inputfile, modelfile, 8, 256, 8.0D)
    Assert.assertTrue(modelfile.exists())
    // now instantiate the NER with modelfile
    val trainedDDNER = new DrugDosageNER(modelfile, true)
    Source.fromFile(inputfile).getLines()
      .foreach(line => {
        val taggedline = trainedDDNER.parse(line)
          .map(taggedWord => 
            taggedWord._1 + "/" + taggedWord._2)
          .mkString(" ")
        Console.println(line)
        Console.println(taggedline)
        Console.println()
    })
  }
  
  @Test
  def testEvaluate(): Unit = {
    val ddNER = new DrugDosageNER(null, true)
    // we use model.train that was generated for internal
    // use during the training phase - this contains the
    // tags from the dict and regex NERs
    val accuracy = ddNER.evaluate(10, 30, 
      new File(datadir, "model.train"), 8, 256, 8.0)
    Console.println("Overall accuracy = " + accuracy)
    Assert.assertTrue(accuracy > 0.5)
  }
}
