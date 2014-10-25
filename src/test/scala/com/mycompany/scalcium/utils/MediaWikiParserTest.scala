package com.mycompany.scalcium.utils

import org.junit.Test
import java.io.File

class MediaWikiParserTest {

  @Test
  def testParseSmallWiki(): Unit = {
    val xmlfile = new File("src/main/resources/wiki/small_wiki.xml")
    val wp = new MediaWikiParser(xmlfile)
    wp.parse()
    val titles = wp.getTitles
    val infoboxes = wp.getInfoboxes
    titles.zip(infoboxes).foreach(nvp => {
      Console.println(">>> title: " + nvp._1)
      nvp._2.keys.foreach(k => Console.println(k + " => " + nvp._2(k)))
    })
  }
}