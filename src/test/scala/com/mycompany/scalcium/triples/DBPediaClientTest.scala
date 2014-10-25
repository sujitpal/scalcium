package com.mycompany.scalcium.triples

import java.io.File

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

import org.junit.Test

import com.mycompany.scalcium.utils.MediaWikiParser

class DBPediaClientTest {

  val people = List("Andrew Johnson", "Edgar Allan Poe", 
      "Desi Arnaz", "Elvis Presley", "Albert Camus", 
      "Arthur Miller", "Boris Yeltsin", "Ernest Hemingway", 
      "Benjamin Franklin", "Bill Oddie", "Abraham Lincoln", 
      "Billy Crystal", "Bill Clinton", "Alfonso V of Aragon", 
      "Dwight D. Eisenhower", "Colin Powell", "Cary Elwes", 
      "Alexander II of Russia", "Arnold Schwarzenegger", 
      "Christopher Columbus", "Barry Bonds", "Bill Gates", 
      "Elizabeth Garrett Anderson")
  
  @Test
  def testExtractSpouseFromInfobox(): Unit = {
    val inParens = new Regex("""\(.*?\)""")
    val xmlfile = new File("src/main/resources/wiki/small_wiki.xml")
    val parser = new MediaWikiParser(xmlfile)
    parser.parse()
    val triples = ArrayBuffer[(String,String,String)]()
    parser.getTitles().zip(parser.getInfoboxes())
      .map(ti => {
        val spouse = if (ti._2.contains("spouse")) ti._2("spouse") else null
        // clean up data received (situation dependent)
        if (spouse != null) {
          val spouses = inParens.replaceAllIn(spouse, "")
            .split("\\s{2,}")
            .map(_.trim)
          spouses.foreach(spouse => 
            triples += ((ti._1, "spouse", spouse)))
        } else triples += ((ti._1, "spouse", "NOTFOUND"))
      })
    triples.foreach(Console.println(_))
  }
  
  @Test
  def testExtractSpouseFromDBPedia(): Unit = {
	val triples = ArrayBuffer[(String,String,String)]()
    val client = new DBPediaClient()
    people.map(person => {
      val spouse = client.getObject(person, "spouse")
      if (spouse != null) {
        // clean up data received (situation dependent)
        val spouses = spouse.replace(',', ' ')
          .replace(')', ' ')
          .split('(')
          .map(s => s.trim())
          .foreach(s => triples += ((person, "spouse", s)))
      } else triples += ((person, "spouse", "NOTFOUND"))
    })
    triples.foreach(Console.println(_))
  }
}