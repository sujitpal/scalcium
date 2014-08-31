package com.mycompany.scalcium.keyextract

import org.junit.Test

class KeaClientTest {

  val trainDir = "/home/sujit/Projects/mlia-examples/data/mtcrawler/kea/train"
  val testDir = "/home/sujit/Projects/mlia-examples/data/mtcrawler/kea/test"
  val modelFile = "/tmp/model"
  val valKeysDir = "/home/sujit/Projects/mlia-examples/data/mtcrawler/kea/test/keys"
    
  @Test
  def testRunKeaClient(): Unit = {
    val kc = new KeaClient()
    kc.train(trainDir, modelFile)
    kc.test(modelFile, testDir)
  }
}