package com.healthline.scalcium.drugdosage

import org.junit.Test
import java.io.File
import scala.io.Source
import java.io.PrintWriter
import java.io.FileWriter

class DrugDosageFSMTest {

  val datadir = "src/main/resources/drug_dosage"
    
  @Test
  def testParse(): Unit = {
    val ddFSM = new DrugDosageFSM(
        new File(datadir, "drugs.dict"),
        new File(datadir, "frequencies.dict"),
        new File(datadir, "routes.dict"),
        new File(datadir, "units.dict"),
        new File(datadir, "num_patterns.dict"),
        false)
    val writer = new PrintWriter(new FileWriter(
      new File(datadir, "fsm_output.txt")))
    Source.fromFile(new File(datadir, "input.txt"))
      .getLines()
      .foreach(line => {
         val stab = ddFSM.parse(line)
         writer.println(line)
         writer.println(stab.map(st => st._2 + "/" + st._1)
           .mkString(" "))
         writer.println()
    })
    writer.flush()
    writer.close()
  }
}
