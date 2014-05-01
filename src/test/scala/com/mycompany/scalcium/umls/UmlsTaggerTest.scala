package com.mycompany.scalcium.umls

import org.junit.Test
import java.io.File
import org.junit.Assert

class UmlsTaggerTest {

//  @Test
//  def testBuild(): Unit = {
//    val input = new File("/home/sujit/Projects/med_data/cuistr1.csv")
//    val output = new File("/home/sujit/Projects/med_data/umlsindex")
//    val tagger = new UmlsTagger()
//    tagger.buildIndex(input, output)
//  }
  
  @Test
  def testMapSingleConcept(): Unit = {
    val luceneDir = new File("/home/sujit/Projects/med_data/umlsindex")
    val tagger = new UmlsTagger()
    val strs = List("Lung Cancer", "Heart Attack", "Diabetes")
    strs.foreach(str => {
      val concepts = tagger.annotateConcepts(str, luceneDir)
      Console.println("Query: " + str)
      tagger.printConcepts(concepts)
      Assert.assertEquals(1, concepts.size)
      Assert.assertEquals(100.0D, concepts.head._1, 0.1D)
    })
  }

  @Test
  def testMapMultipleConcepts(): Unit = {
    val luceneDir = new File("/home/sujit/Projects/med_data/umlsindex")
    val tagger = new UmlsTagger()
    val strs = List(
        "Heart Attack and diabetes",
        "carcinoma (small-cell) of lung",
        "asthma side effects")
    strs.foreach(str => {
      val concepts = tagger.annotateConcepts(str, luceneDir)
      Console.println("Query: " + str)
      tagger.printConcepts(concepts)
    })
  }

//  @Test
//  def testSortWords(): Unit = {
//    val s = "heart attack and diabetes"
//    val tagger = new UmlsTagger()
//    Assert.assertEquals("and attack diabetes heart", tagger.sortWords(s))
//  }
//  
//  @Test
//  def testStemWords(): Unit = {
//    val s = "and attack diabetes heart"
//    val tagger = new UmlsTagger()
//    Assert.assertEquals("attack diabetes heart", tagger.stemWords(s))
//  }
}
