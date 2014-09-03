package com.mycompany.scalcium.names

import org.junit.Test
import org.junit.Assert

class NameFinderTest {

  val sentences = List(
    "Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 .",
    "Mr . Vinken is chairman of Elsevier N.V. , the Dutch publishing group based at Amsterdam .",
    "Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named a director of this British industrial conglomerate ."
  )
  
  @Test
  def testOpenNLPNameFinder(): Unit = {
    val nf = new OpenNLPNameFinder()
    val people = nf.find(nf.personME, sentences).flatMap(x => x)
    Console.println("people=" + people)
    Assert.assertEquals(3, people.size)
    Assert.assertEquals("Pierre Vinken", people.head._1)
    Assert.assertEquals(0, people.head._2)
    Assert.assertEquals(2, people.head._3)
    val companies = nf.find(nf.orgME, sentences).flatMap(x => x)
    Console.println("companies=" + companies)
    Assert.assertEquals(1, companies.size)
    Assert.assertEquals("Consolidated Gold Fields PLC", companies.head._1)
    Assert.assertEquals(10, companies.head._2)
    Assert.assertEquals(14, companies.head._3)
  }
}