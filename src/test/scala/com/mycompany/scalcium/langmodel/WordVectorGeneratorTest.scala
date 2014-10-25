package com.mycompany.scalcium.langmodel

import org.junit.Test
import java.io.File

class WordVectorGeneratorTest {

  @Test
  def testGenerateWordVectors(): Unit = {
    val infile = "src/main/resources/langmodel"
    val outfile = "/tmp/word_vectors.txt"
    val gen = new WordVectorGenerator(new File(infile), new File(outfile))
  }
}