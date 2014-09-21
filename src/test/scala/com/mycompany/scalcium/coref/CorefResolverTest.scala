package com.mycompany.scalcium.coref

import org.junit.Test
import scala.io.Source
import java.io.File

class CorefResolverTest {

  val texts = List(
    "The atom is a basic unit of matter, it consists of a dense central nucleus surrounded by a cloud of negatively charged electrons.",
    "The Revolutionary War occurred during the 1700s and it was the first war in the United States.",
    "Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group.",
    "The project leader is refusing to help. The jerk thinks only of himself.",
    "A final resting place for another legend, Anna Pavlova, the Russian ballerina who spent her final years in London, may be less than secure. For 65 years, Ms. Pavlova's ashes have been in a white urn at Golder's Green cemetery, where they are likely to remain according to a director of the crematorium.",
    "Another icon of the '60s, Che Guevara, has been turned into a capitalist tool 28 years after he was gunned down in Bolivia.",
    "I am Sam. Sam I am. I like green eggs and ham."
  )
      
  @Test
  def testStanfordCorefResolver(): Unit = {
    val scr = CorefResolver.getResolver("stanford")
    texts.foreach(text => {
      val x = scr.resolve(text)
      prettyPrint(text, x)
    })
  }
  
  @Test
  def testLingPipeCorefResolver(): Unit = {
    val lcr = CorefResolver.getResolver("lingpipe")
    texts.foreach(text => {
      val x = lcr.resolve(text)
      prettyPrint(text, x)
    })
//    val bigtext = Source.fromFile(new File("/home/sujit/Downloads/lingpipe-4.1.0/demos/data/johnSmith/0/960114.114"))
//      .getLines().mkString(" ")
//    val x2 = lcr.resolve(bigtext)
//    prettyPrint(bigtext, x2)
  }
  
//  @Test
//  def testOpenNLPCorefResolver(): Unit = {
//    val ocr = CorefResolver.getResolver("opennlp")
//    ocr.resolve(text)
//  }
  
  def prettyPrint(text: String, result: List[(CorefTriple,List[CorefTriple])]) = {
    Console.println(text)
    result.foreach(refcorefs => {
      val ref = refcorefs._1
      val corefs = refcorefs._2
      Console.println("(%d,%d): %s".format(ref.begin, ref.end, ref.text))
      corefs.foreach(coref => 
        Console.println("  (%d,%d): %s".format(coref.begin, coref.end, 
        								       coref.text)))
    })
    Console.println()
  }
}