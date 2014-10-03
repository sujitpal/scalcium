package com.mycompany.scalcium.langmodel

import org.junit.Test
import java.io.File

class WordClusterSOMTest {

  @Test
  def runWordClusterSOMTest(): Unit = {
    val infile = new File("/tmp/word_vectors.txt")
    val outfile = new File("/tmp/word_som.txt")
    val wcs = new WordClusterSOM(infile, outfile)
  }
}