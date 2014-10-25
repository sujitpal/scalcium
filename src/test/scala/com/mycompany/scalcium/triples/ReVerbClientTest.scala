package com.mycompany.scalcium.triples

import java.io.File

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

import org.junit.Test

import com.mycompany.scalcium.utils.MediaWikiParser

class ReVerbClientTest {

  val SpouseWords = Set("wife", "husband")
  val BeWords = Set("is", "was", "be")
  val StopWords = Set("of")

  @Test
  def testExtractTriplesFromXml(): Unit = {
    val reverb = new ReVerbClient()
    val infile = new File("src/main/resources/wiki/small_wiki.xml")
    val parser = new MediaWikiParser(infile)
    parser.parse()
    parser.getTitles.zip(parser.getTexts())
      .map(tt => {
        val title = tt._1
        val triples = reverb.parse(tt._2)
        Console.println(">>> " + title)
        // clean up triple
        val resolvedTriples = triples.map(triple => {
          // resolve pronouns in subj, replace with title
          if (isPronoun(triple._1)) (title, triple._2, triple._3)
          else triple
        })
        val tripleBuf = ArrayBuffer[(String,String,String)]()
        // filter out where verb is (married, divorced)
        tripleBuf ++= resolvedTriples.filter(triple => {
          (triple._2.indexOf("married") > -1 || 
            triple._2.indexOf("divorced") > -1)
        })
        // filter out where subj or obj has (wife, husband)
        // and the verb is (is, was, be)
        tripleBuf ++= resolvedTriples.filter(triple => {
          val wordsInSubj = triple._1.split("\\s+").map(_.toLowerCase).toSet
          val wordsInVerb = triple._2.split("\\s+").map(_.toLowerCase).toSet
          val wordsInObj = triple._3.split("\\s+").map(_.toLowerCase).toSet
          (wordsInSubj.intersect(SpouseWords).size > 0 &&
            wordsInVerb.intersect(BeWords).size > 0 &&
            isProperNoun(triple._3)) ||
          (isProperNoun(triple._1) &&
            wordsInVerb.intersect(BeWords).size > 0 &&
            wordsInObj.intersect(SpouseWords).size > 0)
        })
        // extract patterns like "Bill and Hillary Clinton" from either
        // subj or obj
        tripleBuf ++= resolvedTriples.map(triple => {
            val names = title.split("\\s+")
            val pattern = new Regex("""%s (and|&) (\w+) %s"""
              .format(names(0), names(names.size - 1)))
            val sfName = pattern.findAllIn(triple._1).matchData
                .map(m => m.group(2)).toList ++
              pattern.findAllIn(triple._3).matchData
                .map(m => m.group(2)).toList
            if (sfName.size == 1)
              (title, "spouse", List(sfName.head, 
                names(names.size - 1)).mkString(" "))
            else ("x", "x", "x")
          })
          .filter(triple => "spouse".equals(triple._2))
        // post-process the triples
        val spouseTriples = tripleBuf.map(triple => {
            // fix incomplete name in subj and obj from title
            val subjHasTitle = containsTitle(triple._1, title)
            val objHasTitle = containsTitle(triple._3, title)
            val subj = if (subjHasTitle) title else triple._1
            val obj = if (objHasTitle && !subjHasTitle) title else triple._3
            val verb = if (subjHasTitle || objHasTitle) "spouse" else triple._2
            (subj, verb, obj)
          })
          .filter(triple => 
            // make sure both subj and obj are proper nouns
            (isProperNoun(triple._1) && 
              "spouse".equals(triple._2) &&
              isProperNoun(triple._3)))
          
        spouseTriples.foreach(Console.println(_))
      })
  }
  
  def isPronoun(s: String): Boolean =
    "she".equalsIgnoreCase(s) || "he".equalsIgnoreCase(s)
    
  def isProperNoun(s: String): Boolean = {
    val words = s.split("\\s+").filter(w => !StopWords.contains(w))
    val initCapWords = words.filter(w => w.charAt(0).isUpper == true)
    words.length == initCapWords.length
  }
  
  def containsTitle(s: String, title: String): Boolean = {
    val words = Set() ++ s.split("\\s+") 
    val names = Set() ++ title.split("\\s+")
    words.intersect(names).size > 0
  }
}
