package com.healthline.scalcium.smoke

import java.util.regex.Pattern
import org.apache.lucene.analysis.core.StopAnalyzer
import com.healthline.scalcium.utils.Tokenizer

class Preprocessor {

  val punctPattern = Pattern.compile("\\p{Punct}")
  val spacePattern = Pattern.compile("\\s+")
  val classPattern = Pattern.compile("class")
  
  val stopwords = StopAnalyzer.ENGLISH_STOP_WORDS_SET
  val tokenizer = Tokenizer.getTokenizer("opennlp")

  val multiClassTargets = Map(
    ("CURRENT SMOKER", 0),
    ("NON-SMOKER", 1),
    ("PAST SMOKER", 2),
    ("SMOKER", 3),
    ("UNKNOWN", 4))
  val smokerNonSmokerTargets = Map(
    ("CURRENT SMOKER", 1),
    ("PAST SMOKER", 1),
    ("SMOKER", 1),
    ("NON-SMOKER", 0),
    ("UNKNOWN", 0))
  val smokerSubTargets = Map(
    ("CURRENT SMOKER", 0),
    ("PAST SMOKER", 1),
    ("SMOKER", 2))
  val nonSmokerSubTargets = Map(
    ("NON-SMOKER", 0),
    ("UNKNOWN", 1))

  def preprocess(sin: String): String = {
    val sinClean = replaceAll(classPattern, "clazz",
      replaceAll(spacePattern, " ",
      replaceAll(punctPattern, " ", sin.toLowerCase())))
    // stopword removal
    val sinStp = tokenizer.wordTokenize(sinClean)
      .filter(word => !stopwords.contains(word))
      .mkString(" ")
//    // noun-phrase chunking
//    val sinNps = tokenizer.phraseChunk(sinClean)
//      .filter(pair => "NP".equals(pair._2))
//      .map(pair => pair._1.replaceAll(" ", "_"))
//      .mkString(" ")
    sinStp
  }
  
  def replaceAll(pattern: Pattern, 
      replacement: String, 
      input: String): String = {
    pattern.matcher(input)
      .replaceAll(replacement)
  }
}