package com.mycompany.scalcium.smoke

import java.io.File
import java.io.PrintWriter
import java.io.FileWriter

class ArffBuilder {

  def buildMulticlassArff(xmlin: File, 
      arffout: File): Unit = {
    val preprocessor = new Preprocessor()
    val arffWriter = openWriter(arffout)
    writeHeader(arffWriter, preprocessor.multiClassTargets)
    val rootElem = scala.xml.XML.loadFile(xmlin)
    (rootElem \ "RECORD").map(record => {
      val smoking = (record \ "SMOKING" \ "@STATUS").text
      val text = (record \ "TEXT").text
        .split("\n")
        .filter(line => line.endsWith("."))
        .map(line => preprocessor.preprocess(line))
        .mkString(" ")
      writeData(arffWriter, smoking, text,
        preprocessor.multiClassTargets)
    })
    closeWriter(arffWriter)
  }
  
  def buildSmokerNonSmokerArff(xmlin: File, 
      arffout: File): Unit = {
    val preprocessor = new Preprocessor()
    val arffWriter = openWriter(arffout)
    writeHeader(arffWriter, 
      preprocessor.smokerNonSmokerTargets)
    val rootElem = scala.xml.XML.loadFile(xmlin)
    (rootElem \ "RECORD").map(record => {
      val smoking = (record \ "SMOKING" \ "@STATUS").text
      val text = (record \ "TEXT").text
        .split("\n")
        .filter(line => line.endsWith("."))
        .map(line => preprocessor.preprocess(line))
        .mkString(" ")
      writeData(arffWriter, smoking, text, 
        preprocessor.smokerNonSmokerTargets)
    })
    closeWriter(arffWriter)
  }
  
  def buildSubClassifierArrfs(xmlin: File,
      smokSubArff: File, nonSmokArff: File): Unit = {
    val preprocessor = new Preprocessor()
    val smokArffWriter = openWriter(smokSubArff)
    val nonSmokArffWriter = openWriter(nonSmokArff)
    writeHeader(smokArffWriter, 
      preprocessor.smokerSubTargets)
    writeHeader(nonSmokArffWriter, 
      preprocessor.nonSmokerSubTargets)
    val rootElem = scala.xml.XML.loadFile(xmlin)
    (rootElem \ "RECORD").map(record => {
      val smoking = (record \ "SMOKING" \ "@STATUS").text
      val text = (record \ "TEXT").text
        .split("\n")
        .filter(line => line.endsWith("."))
        .map(line => preprocessor.preprocess(line))
        .mkString(" ")
      if (preprocessor.smokerNonSmokerTargets(smoking) == 1)
        writeData(smokArffWriter, smoking, text, 
          preprocessor.smokerSubTargets)
      else 
        writeData(nonSmokArffWriter, smoking, text, 
          preprocessor.nonSmokerSubTargets)
    })
    closeWriter(smokArffWriter)
    closeWriter(nonSmokArffWriter)
  }
  
  def openWriter(f: File): PrintWriter = 
    new PrintWriter(new FileWriter(f))
  
  def writeHeader(w: PrintWriter, 
      targets: Map[String,Int]): Unit = {
    val classes = targets.map(kv => kv._2)
                         .toSet
                         .toList
                         .sortWith(_ < _)
                         .mkString(",")
    w.println("@relation smoke")
    w.println()
    w.println("@attribute class {%s}".format(classes)) 
    w.println("@attribute text string")
    w.println()
    w.println("@data")
  }
  
  def writeData(w: PrintWriter, 
      smoking: String, body: String,
      targets: Map[String,Int]): Unit = {
    w.println("%d,\"%s\"".format(
      targets(smoking), body))
  }
  
  def closeWriter(w: PrintWriter): Unit = {
    w.flush()
    w.close()
  }
}
