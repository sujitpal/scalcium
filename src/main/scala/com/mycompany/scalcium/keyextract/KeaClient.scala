package com.mycompany.scalcium.keyextract

import java.io.File
import kea.main.KEAModelBuilder
import kea.stemmers.PorterStemmer
import kea.stopwords.StopwordsEnglish
import kea.main.KEAKeyphraseExtractor

object KeaClient extends App {

  val trainDir = "/Users/palsujit/Projects/med_data/mtcrawler/kea/train"
  val testDir = "/Users/palsujit/Projects/med_data/mtcrawler/kea/test"
  val modelFile = "/tmp/model"
  val valKeysDir = "/Users/palsujit/Projects/med_data/mtcrawler/kea/test/keys"
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
    keyExtractor.setNumPhrases(40)
    keyExtractor.setBuildGlobal(true)
    keyExtractor.loadModel()
    keyExtractor.extractKeyphrases(keyExtractor.collectStems())
  }
}
