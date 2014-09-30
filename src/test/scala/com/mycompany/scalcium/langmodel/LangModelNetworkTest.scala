package com.mycompany.scalcium.langmodel

import org.junit.Test
import java.io.File

class LangModelNetworkTest {

  @Test
  def testReadSentences(): Unit = {
    val infile = new File("src/main/resources/langmodel/raw_sentences.txt")
    val lmn = new LangModelNetwork(infile)
  }
}