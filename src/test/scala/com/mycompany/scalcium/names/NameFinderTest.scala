package com.mycompany.scalcium.names

import org.junit.Test
import org.junit.Assert

class NameFinderTest {

  val sentences = List(
    "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29.",
    "Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group based at Amsterdam.",
    "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC, was named a director of this British industrial conglomerate."
  )
  
  @Test
  def testOpenNLPNameFinder(): Unit = {
    val nf = NameFinder.getNameFinder("opennlp")
    val names = nf.find(sentences)
    prettyPrint("OpenNLP", sentences, names)
    Assert.assertEquals(3, names.size)
    Assert.assertEquals(1, names(0).size)
    Assert.assertEquals("PERSON", names(0)(0)._1)
    Assert.assertEquals(0, names(0)(0)._2)
    Assert.assertEquals(13, names(0)(0)._3)
    Assert.assertEquals("Pierre Vinken", 
      sentences(0).substring(names(0)(0)._2, 
      names(0)(0)._3))
  }
  
  @Test
  def testStanfordNameFinder(): Unit = {
    val nf = NameFinder.getNameFinder("stanford")
    val names = nf.find(sentences)
    prettyPrint("Stanford", sentences, names)
    Assert.assertEquals(3, names.size)
    Assert.assertEquals(3, names(0).size)
    Assert.assertEquals("PERSON", names(0)(0)._1)
    Assert.assertEquals(0, names(0)(0)._2)
    Assert.assertEquals(13, names(0)(0)._3)
    Assert.assertEquals("Pierre Vinken", 
      sentences(0).substring(names(0)(0)._2, 
      names(0)(0)._3))
  }
  
  def prettyPrint(nername: String,
      sentences: List[String], 
      names: List[List[(String,Int,Int)]]): Unit = {
    Console.println("==== " + nername + " ====")
    sentences.zip(names).foreach(sn => {
      Console.println(sn._1)
      sn._2.foreach(n => 
        Console.println("  (" + n._2 + "," + n._3 + 
          "): " + sn._1.substring(n._2, n._3) + 
          " / " + n._1))
    })
  }
}