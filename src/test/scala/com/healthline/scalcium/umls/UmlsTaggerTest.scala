package com.healthline.scalcium.umls

import org.junit.Test
import java.io.File

class UmlsTaggerTest {

  @Test
  def testBuild(): Unit = {
    val input = new File("/home/sujit/Projects/med_data/cuistr_100.csv")
    val output = new File("/home/sujit/med_data/umlsindex")
    val tagger = new UmlsTagger()
    tagger.build(input, output)
  }
}
