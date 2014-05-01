package com.mycompany.scalcium.utils

import org.junit.Test
import org.junit.Assert
import org.junit.Test

class TokenizerTest {

  val text = "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29. Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group. Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a nonexecutive director of this British industrial conglomerate. A form of asbestos once used to make Kent cigarette filters has caused a high percentage of cancer deaths among a group of workers exposed to it more than 30 years ago, researchers reported. The asbestos fiber, crocidolite, is unusually resilient once it enters the lungs, with even brief exposures to it causing symptoms that show up decades later, researchers said."
  val openNlpTokenizer = Tokenizer.getTokenizer("opennlp")
  val lingPipeTokenizer = Tokenizer.getTokenizer("lingpipe")
  
  @Test def testSentTokenize(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    sentences.foreach(Console.println(_))
    Assert.assertEquals(5, sentences.length)
    Console.println("--")
    // LingPipe
    val sentences2 = lingPipeTokenizer.sentTokenize(text)
    sentences2.foreach(Console.println(_))
    Assert.assertEquals(5, sentences2.length)
    Console.println()
  }
  
  @Test def testWordTokenize(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    val words = openNlpTokenizer.wordTokenize(sentences(0))
    words.foreach(Console.println(_))
    Assert.assertEquals(18, words.length)
    Console.println("--")
    // LingPipe
    // minor difference in tokenization wrt trailing periods
    // in sentences, eg Nov.
    val sentences2 = lingPipeTokenizer.sentTokenize(text)
    val words2 = lingPipeTokenizer.wordTokenize(sentences2(0))
    words2.foreach(Console.println(_))
    Assert.assertEquals(19, words2.length)
    Console.println()
  }
  
  @Test def testPhraseTokenize(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    val phrases = openNlpTokenizer.phraseTokenize(sentences(0))
    phrases.foreach(Console.println(_))
    Assert.assertEquals(8, phrases.length)
    Console.println("--")
    // LingPipe
    // minor difference in chunking, LingPipe does a better job
    val sentences2 = lingPipeTokenizer.sentTokenize(text)
    val phrases2 = lingPipeTokenizer.phraseTokenize(sentences2(0))
    phrases2.foreach(Console.println(_))
    Assert.assertEquals(5, phrases2.length)
    Console.println("--")
  }
  
  @Test def testPhraseChunk(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    val chunks = openNlpTokenizer.phraseChunk(sentences(0))
    chunks.foreach(Console.println(_))
    Assert.assertEquals("Pierre Vinken", chunks(0)._1)
    Assert.assertEquals("NP", chunks(0)._2)
    Console.println("--")
    // LingPipe
    // HERE BE DRAGONS! LingPipe returns the the full names
    // for phrase types and brown tags for POS tags, while
    // OpenNLP uses Brown tags for both. Code that needs to
    // selectively work with NP/VP/ADJP/... will need to 
    // filter for BOTH strings.
    val sentences2 = lingPipeTokenizer.sentTokenize(text)
    val chunks2 = lingPipeTokenizer.phraseChunk(sentences2(0))
    chunks2.foreach(Console.println(_))
    Assert.assertEquals("Pierre Vinken", chunks2(0)._1)
    Assert.assertEquals("noun", chunks2(0)._2)
    Console.println()
  }
  
  @Test def testPosTag(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    val taggedWords = openNlpTokenizer.posTag(sentences(0))
    taggedWords.foreach(Console.println(_))
    Assert.assertEquals("Pierre", taggedWords(0)._1)
    Assert.assertEquals("NNP", taggedWords(0)._2)
    Console.println("--")
    // LingPipe
    val sentences2 = openNlpTokenizer.sentTokenize(text)
    val taggedWords2 = openNlpTokenizer.posTag(sentences2(0))
    taggedWords2.foreach(Console.println(_))
    Assert.assertEquals("Pierre", taggedWords2(0)._1)
    Assert.assertEquals("NNP", taggedWords2(0)._2)
    Console.println()
  }
}
