package com.healthline.scalcium.utils

import org.junit.Test
import org.junit.Assert
import org.junit.Test

class TokenizerTest {

  val text = "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29. Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group. Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a nonexecutive director of this British industrial conglomerate. A form of asbestos once used to make Kent cigarette filters has caused a high percentage of cancer deaths among a group of workers exposed to it more than 30 years ago, researchers reported. The asbestos fiber, crocidolite, is unusually resilient once it enters the lungs, with even brief exposures to it causing symptoms that show up decades later, researchers said."
  val tokenizer = new Tokenizer()
  
  @Test def testSentTokenize(): Unit = {
    val sentences = tokenizer.sentTokenize(text)
    sentences.foreach(Console.println(_))
    Assert.assertEquals(5, sentences.length)
  }
  
  @Test def testWordTokenize(): Unit = {
    val sentences = tokenizer.sentTokenize(text)
    val words = tokenizer.wordTokenize(sentences(0))
    words.foreach(Console.println(_))
    Assert.assertEquals(18, words.length)
  }
  
  @Test def testPhraseTokenize(): Unit = {
    val sentences = tokenizer.sentTokenize(text)
    val phrases = tokenizer.phraseTokenize(sentences(0))
    phrases.foreach(Console.println(_))
    Assert.assertEquals(8, phrases.length)
  }
  
  @Test def testPhraseChunk(): Unit = {
    val sentences = tokenizer.sentTokenize(text)
    val chunks = tokenizer.phraseChunk(sentences(0))
    chunks.foreach(Console.println(_))
    Assert.assertEquals(chunks(0)._1, "Pierre Vinken")
    Assert.assertEquals(chunks(0)._2, "NP")
  }
  
  @Test def testPosTag(): Unit = {
    val sentences = tokenizer.sentTokenize(text)
    val taggedWords = tokenizer.posTag(sentences(0))
    taggedWords.foreach(Console.println(_))
    Assert.assertEquals(taggedWords(0)._1, "Pierre")
    Assert.assertEquals(taggedWords(0)._2, "NNP")
  }
}