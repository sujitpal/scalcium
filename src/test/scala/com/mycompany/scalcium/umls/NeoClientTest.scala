package com.mycompany.scalcium.umls

import org.junit.Test
import org.junit.Assert
import java.io.File

class NeoClientTest {

  val client = new NeoClient()
  
  @Test
  def testGetCuiById(): Unit = {
    val cui = client.getCuiById(5911)
    Console.println("cui=" + cui)
    Assert.assertNotNull(cui)
  }
  
  @Test
  def testGetConceptById(): Unit = {
    client.getConceptById(5911) match {
      case Some(concept) => {
        printConcept(concept)
        Assert.assertNotNull(concept)
      }
      case None => Assert.fail("No concept found?")
    }
  }
  
  @Test
  def testGetConceptByCui(): Unit = {
    client.getConceptByCui("C0027051") match {
      case Some(concept) => {
        printConcept(concept)
        Assert.assertEquals(concept.cui, "C0027051")
        Assert.assertEquals(concept.syns.size, 57)
        Assert.assertEquals(concept.stys.size, 1)
      }
      case None => Assert.fail("No concept found?")
    }
  }

  @Test
  def testListRelationships(): Unit = {
    val rels = client.listRelationships("C0027051")
    Console.println("relations:" + rels)
    Assert.assertNotNull(rels)
    Assert.assertEquals(34, rels.size)
  }
  
  @Test
  def testListRelatedConcepts(): Unit = {
    val relcs = client.listRelatedConcepts(
      "C0027051", ":drug_contraindicated_for")
    Console.println("related concepts:" + relcs)
    Assert.assertNotNull(relcs)
    Assert.assertEquals(389, relcs.size)
  }

  @Test
  def testShortestPath(): Unit = {
    val shortestPath = client.shortestPath("C0027051", "C0011847", 5)
    Console.println("nodelist=" + shortestPath._1)
    Assert.assertNotNull(shortestPath._1)
    Assert.assertEquals(3, shortestPath._1.size)
    Console.println("rellist=" + shortestPath._2)
    Assert.assertNotNull(shortestPath._2)
    Assert.assertEquals(2, shortestPath._2.size)
    Assert.assertEquals(2, shortestPath._3)
  }
  
  @Test
  def testDegreeDist(): Unit = {
    val fin = new File("/tmp/dd_in.csv")
    client.degreeDistrib(true, fin)
    val fout = new File("/tmp/dd_out.csv")
    client.degreeDistrib(false, fout)
  }
  
  def printConcept(concept: Concept): Unit = {
    Console.println(
      "==== Concept (cui=%s) ====".format(concept.cui))
    Console.println("name: %s".format(concept.syns.head))
    Console.println("#-syns: %d".format(concept.syns.tail.size))
    Console.println("sty-codes: %s".format(concept.stys.mkString(", ")))
  }
}
