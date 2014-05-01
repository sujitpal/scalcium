package com.healthline.scalcium.transformers

import scala.Array.canBuildFrom
import scala.util.matching.Regex

import com.healthline.scalcium.utils.Tokenizer

/**
 * Operates on the entire document and replaces in-document
 * abbreviations with the full-form before processing. Looks
 * for patterns with (.*) preceded by a string with the same
 * number of words as the length of the abbreviation and where
 * the first character of each word matches each character. 
 */
object AbbreviationExpander {

  val AbbrPattern = new Regex("\\(.*?\\)")
  val PunctStripper = new Regex("\\p{Punct}")
  
  def expand(text: String, tokenizer: Tokenizer): String = {
    val abbrMap = scala.collection.mutable.Map[String,String]()
    val matches = AbbrPattern.findAllIn(text).map(m => 
      PunctStripper.replaceAllIn(m, "")).toList
    val sentences = tokenizer.sentTokenize(text)
    val plainSentences = sentences.map(sentence => 
      PunctStripper.replaceAllIn(sentence, ""))
    for (m <- matches; 
         s <- plainSentences) {
      if (! abbrMap.contains(m)) {
        val words = s.split(" ")
        val pos = words.indexOf(m)
        val len = m.length()
        if (pos - len >= 0) {
          val candidateExpansion = words.slice(pos - len, pos)
          if (isExpansionValid(candidateExpansion, m)) 
            abbrMap(m) = candidateExpansion.mkString(" ")
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

  def isExpansionValid(words: Array[String], abbr: String): Boolean = {
    words.zip(abbr).forall(x => x._1.charAt(0) == x._2)
  }
}