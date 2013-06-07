package com.healthline.scalcium

import scala.util.matching.Regex

/**
 * Operates on the entire document and replaces in-document
 * abbreviations with the full-form before processing. Looks
 * for patterns with (.*) preceded by a string with the same
 * number of words as the length of the abbreviation and where
 * the first character of each word matches each character. 
 */
object AbbreviationExpander {

  def expandAbbreviations(text: String, tokenizer: Tokenizer): String = {
    val pattern = new Regex("\\(.*?\\)")
    val punct = new Regex("\\p{Punct}")
    val abbrMap = scala.collection.mutable.Map[String,String]()
    val matches = pattern.findAllIn(text).map(m => 
      punct.replaceAllIn(m, "")).toList
    val sentences = tokenizer.sentTokenize(text)
    val plainSentences = sentences.map(sentence => 
      punct.replaceAllIn(sentence, ""))
    for (m <- matches; 
         s <- plainSentences) {
      if (! abbrMap.contains(m)) {
        val words = s.split(" ")
        val pos = words.indexOf(m)
        val len = m.length()
        if (pos - len >= 0) {
          val candidate = words.slice(pos - len, pos)
          if (isValid(candidate, m)) abbrMap(m) = candidate.mkString(" ")
        }
      }
    }
    var otext = text
    for ((k, v) <- abbrMap) {
      otext = otext.replaceAll(k, v)
    }
    for ((k, v) <- abbrMap) {
      val fv = "\\(" + v + "\\)"
      otext = otext.replaceFirst(fv, "")
    }
    otext
  }

  def isValid(words: Array[String], abbr: String): Boolean = {
    words.zip(abbr.toCharArray()).forall(
      x => x._1.toLowerCase.charAt(0) == x._2.toLowerCase)
  }
}