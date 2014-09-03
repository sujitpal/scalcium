package com.mycompany.scalcium.tokenizers

object Tokenizer {
  def getTokenizer(name: String): Tokenizer = {
    if ("opennlp".equalsIgnoreCase(name)) new OpenNLPTokenizer()
    else new LingPipeTokenizer()
  }
}

trait Tokenizer {

  /**
   * Tokenize incoming text into List of paragraphs.
   */
  def paraTokenize(text: String): List[String] = {
    text.split("\n\n").toList  
  }
    
  /**
   * Tokenize a paragraph into a List of sentences.
   */
  def sentTokenize(para: String): List[String]

  /**
   * Tokenize sentence into a List of phrases.
   */
  def phraseTokenize(sentence: String): List[String] = {
    phraseChunk(sentence).map(x => x._1)
  }

  /**
   * Tokenize sentence into List of words.
   */
  def wordTokenize(sentence: String): List[String]

  /**
   * POS tags a sentence into a list of (token, tag) tuples.
   */
  def posTag(sentence: String): List[(String,String)]

  /**
   * Shallow chunks a sentence into a list of (phrase, tag)
   * tuples.
   */
  def phraseChunk(sentence: String): List[(String,String)]
}
