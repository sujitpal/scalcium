package com.mycompany.scalcium.keyextract

import java.io.File
import kea.main.KEAModelBuilder
import kea.stemmers.PorterStemmer
import kea.stopwords.StopwordsEnglish
import kea.main.KEAKeyphraseExtractor

object KeaClient extends App {

  val trainDir = "/home/sujit/Projects/mlia-examples/data/mtcrawler/kea/train"
  val testDir = "/home/sujit/Projects/mlia-examples/data/mtcrawler/kea/test"
  val modelFile = "/tmp/model"
  val valKeysDir = "/home/sujit/Projects/mlia-examples/data/mtcrawler/kea/test/keys"
  val kc = new KeaClient()
  kc.train(trainDir, modelFile)
  kc.test(modelFile, testDir)
}

class KeaClient {

  def train(trainDir: String, modelFilePath: String): Unit = {
    val modelBuilder = new KEAModelBuilder()
    modelBuilder.setDirName(trainDir)
    modelBuilder.setModelName(modelFilePath)
    modelBuilder.setVocabulary("none")
    modelBuilder.setEncoding("UTF-8")
    modelBuilder.setDocumentLanguage("en")
    modelBuilder.setStemmer(new PorterStemmer())
    modelBuilder.setStopwords(new StopwordsEnglish())
    modelBuilder.setMaxPhraseLength(5)
    modelBuilder.setMinPhraseLength(1)
    modelBuilder.setMinNumOccur(2)
    modelBuilder.buildModel(modelBuilder.collectStems())
    modelBuilder.saveModel()
  }
  
  def test(modelFilePath: String, testDir: String): Unit = {
    val keyExtractor = new KEAKeyphraseExtractor()
    keyExtractor.setDirName(testDir)
    keyExtractor.setModelName(modelFilePath)
    keyExtractor.setVocabulary("none")
    keyExtractor.setEncoding("UTF-8")
    keyExtractor.setDocumentLanguage("en")
    keyExtractor.setStemmer(new PorterStemmer())
    keyExtractor.setNumPhrases(10)
    keyExtractor.setBuildGlobal(true)
    keyExtractor.loadModel()
    keyExtractor.extractKeyphrases(keyExtractor.collectStems())
  }
}