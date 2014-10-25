package com.mycompany.scalcium.utils

import java.io.File

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._
import scala.xml.XML

import info.bliki.wiki.dump.WikiPatternMatcher
import info.bliki.wiki.model.WikiModel
import net.htmlparser.jericho.Source

class MediaWikiParser(xmlfile: File) {

  val InfoboxStartPattern = "{{Infobox"
    
  val titles = ArrayBuffer[String]()
  val texts = ArrayBuffer[String]()
  val infoboxes = ArrayBuffer[Map[String,String]]()
  
  def parse(): Unit = {
    val mediaWikiElement = XML.loadFile(xmlfile)
    (mediaWikiElement \ "page").map(pageElement => {
      val title = (pageElement \ "title").text
      val text = (pageElement \ "revision" \ "text").text
      // parse out Infobox
      val infoboxStart = text.indexOf(InfoboxStartPattern)
      var bcount = 2
      var infoboxEnd = infoboxStart + InfoboxStartPattern.length()
      breakable {
        (infoboxEnd until text.length()).foreach(i => { 
          val c = text.charAt(i)
          var binc = 0
          if (c == '}') binc = -1
          else if (c == '{') binc = 1
          else binc = 0
          bcount += binc
          infoboxEnd = i
          if (bcount == 0) break
        })
      }
      if (infoboxStart >= 0) {
        addTitle(title)
        addInfobox(text.substring(infoboxStart, infoboxEnd))
        addText(text.substring(infoboxEnd + 1))
      }
    })
  }
  
  def addTitle(title: String): Unit = titles += title

  def addInfobox(ibtext: String): Unit = {
    val infobox = ibtext.split("\n")
      .map(line => {
        val pipePos = line.indexOf('|')
        val nvp = line.substring(pipePos + 1).split("=")
        if (nvp.length == 2) {
          val wpm = new WikiPatternMatcher(nvp(1).trim())
          (nvp(0).trim(), wpm.getPlainText())
        } 
        else ("None", "")
      })
      .filter(nvp => ! "None".equals(nvp._1))
      .toMap
    infoboxes += infobox
  }
  
  def addText(text: String): Unit = {
    // convert wiki text to HTML, then to plain text
    val htmltext = WikiModel.toHtml(text)
    val plaintext = new Source(htmltext).getTextExtractor().toString()
    texts += plaintext
  }
  
  def getTitles(): List[String] = titles.toList
  
  def getInfoboxes(): List[Map[String,String]] = infoboxes.toList
  
  def getTexts(): List[String] = texts.toList
}