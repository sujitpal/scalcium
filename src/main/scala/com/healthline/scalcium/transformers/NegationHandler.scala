package com.healthline.scalcium.transformers

import scala.io.Source
import scala.util.control.Breaks.{break, breakable}

import com.healthline.scalcium.utils.Tokenizer

object NegationHandler {

  val NegationPrefix = "negate0"
    
  val phraseSrc = Source.fromInputStream(getClass.getResourceAsStream("/negator_phrases.txt"))
  val phrasePatterns = phraseSrc.getLines.filter(line => 
    ((line.trim().length() > 0) && (! line.startsWith("#")))).
    toList.
    sortWith(_.length > _.length).
    map(phrase => pad(phrase))
  
  def maskNegative(sentence: String, tokenizer: Tokenizer): String = {
    var splits: (String,String) = null
    breakable {
      for (phrasePattern <- phrasePatterns) {
        val psentence = pad(sentence)
        val firstMatch = psentence.toLowerCase().indexOf(phrasePattern)
        if (firstMatch > -1) {
          splits = psentence.splitAt(firstMatch)
          break
        }
      }
    }
    if (splits == null) sentence
    else {
      val nright = tokenizer.wordTokenize(splits._2).map(word => 
        NegationPrefix + word).mkString(" ")
      splits._1 + " " + nright
    }
  }
  
  def pad(s: String): String = " " + s + " "
}