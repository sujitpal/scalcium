package com.mycompany.scalcium.tokenizers

import org.junit.Test
import org.junit.Assert
import org.junit.Test

class TokenizerTest {

  val text = "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29. Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group. Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a nonexecutive director of this British industrial conglomerate. A form of asbestos once used to make Kent cigarette filters has caused a high percentage of cancer deaths among a group of workers exposed to it more than 30 years ago, researchers reported. The asbestos fiber, crocidolite, is unusually resilient once it enters the lungs, with even brief exposures to it causing symptoms that show up decades later, researchers said."
  val openNlpTokenizer = Tokenizer.getTokenizer("opennlp")
  val lingPipeTokenizer = Tokenizer.getTokenizer("lingpipe")
  val stanfordTokenizer = Tokenizer.getTokenizer("stanford")
  
  @Test 
  def testSentTokenizeOpenNLP(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    Console.println("Sentence Tokenize :: OpenNLP")
    sentences.foreach(Console.println(_))
    Assert.assertEquals(5, sentences.length)
  }
    
  @Test
  def testSentTokenizeLingPipe(): Unit = {
    // LingPipe
    val sentences = lingPipeTokenizer.sentTokenize(text)
    Console.println("Sentence Tokenize :: LingPipe")
    sentences.foreach(Console.println(_))
    Assert.assertEquals(5, sentences.length)
  }
  
  @Test
  def testSentTokenizeStanford(): Unit = {
    // Stanford
    val sentences = stanfordTokenizer.sentTokenize(text)
    Console.println("Sentence Tokenize :: Stanford")
    sentences.foreach(Console.println(_))
    Assert.assertEquals(5, sentences.length)
  }
  
  @Test 
  def testWordTokenizeOpenNLP(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    val words = openNlpTokenizer.wordTokenize(sentences(0))
    Console.println("Word Tokenize :: OpenNLP")
    words.foreach(Console.println(_))
    Assert.assertEquals(18, words.length)
  }
  
  @Test
  def testWordTokenizeLingPipe(): Unit = {
    // LingPipe
    // minor difference in tokenization wrt trailing periods
    // in sentences, eg Nov.
    val sentences = lingPipeTokenizer.sentTokenize(text)
    val words = lingPipeTokenizer.wordTokenize(sentences(0))
    Console.println("Word Tokenize :: LingPipe")
    words.foreach(Console.println(_))
    Assert.assertEquals(19, words.length)
  }
  
  @Test
  def testWordTokenizeStanford(): Unit = {
    val sentences = stanfordTokenizer.sentTokenize(text)
    val words = stanfordTokenizer.wordTokenize(sentences(0))
    Console.println("Word Tokenize :: Stanford")
    words.foreach(Console.println(_))
    Assert.assertEquals(18, words.length)
  }
  
  @Test 
  def testPhraseTokenizeOpenNLP(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    val phrases = openNlpTokenizer.phraseTokenize(sentences(0))
    Console.println("Phrase Tokenize :: OpenNLP")
    phrases.foreach(Console.println(_))
    Assert.assertEquals(8, phrases.length)
  }
  
  @Test
  def testPhraseTokenizeLingPipe(): Unit = {
    // LingPipe
    // minor difference in chunking, LingPipe does a better job
    val sentences = lingPipeTokenizer.sentTokenize(text)
    val phrases = lingPipeTokenizer.phraseTokenize(sentences(0))
    Console.println("Phrase Tokenize :: LingPipe")
    phrases.foreach(Console.println(_))
    Assert.assertEquals(5, phrases.length)
  }
  
  @Test
  def testPhraseTokenizeStanford(): Unit = {
    // Stanford
    val sentences = stanfordTokenizer.sentTokenize(text)
    val phrases = stanfordTokenizer.phraseTokenize(sentences(0))
    Console.println("Phrase Tokenize :: Stanford")
    phrases.foreach(Console.println(_))
    Assert.assertEquals(11, phrases.length)
  }
  
  @Test 
  def testPhraseChunkOpenNLP(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    val chunks = openNlpTokenizer.phraseChunk(sentences(0))
    Console.println("Phrase Chunk :: OpenNLP")
    chunks.foreach(Console.println(_))
    Assert.assertEquals("Pierre Vinken", chunks(0)._1)
    Assert.assertEquals("NP", chunks(0)._2)
  }
  
  @Test
  def testPhraseChunkLingPipe(): Unit = {
    // LingPipe
    // HERE BE DRAGONS! LingPipe returns the the full names
    // for phrase types and brown tags for POS tags, while
    // OpenNLP uses Brown tags for both. Code that needs to
    // selectively work with NP/VP/ADJP/... will need to 
    // filter for BOTH strings.
    // Another issue: phrase chunks are not returned in the
    // order they occur in the sentence.
    val sentences = lingPipeTokenizer.sentTokenize(text)
    val chunks = lingPipeTokenizer.phraseChunk(sentences(0))
    Console.println("Phrase Chunk :: LingPipe")
    chunks.foreach(Console.println(_))
    val pvchunk = chunks.filter(chunk => "Pierre Vinken".equals(chunk._1))
    Assert.assertEquals(1, pvchunk.size)
    Assert.assertEquals("noun", pvchunk.head._2)
  }
  
  @Test
  def testPhraseChunkStanford(): Unit = {
    // Stanford parser does only deep parsing, so StanfordTokenizer's
    // phraseChunk method parses the tree into a flat list of phrase
    // chunks, so phrases extracted are different from OpenNLP's but
    // correct according to the parse of the sentence.
    //(ROOT
	//  (S
	//    (NP
	//      (NP (NNP Pierre) (NNP Vinken))
	//      (, ,)
	//      (ADJP
	//        (NP (CD 61) (NNS years))
	//        (JJ old))
	//      (, ,))
	//    (VP (MD will)
	//      (VP (VB join)
	//        (NP (DT the) (NN board))
	//        (PP (IN as)
	//          (NP (DT a) (JJ nonexecutive) (NN director)))
	//        (NP-TMP (NNP Nov.) (CD 29))))
	//    (. .)))
    val sentences = stanfordTokenizer.sentTokenize(text)
    val chunks = stanfordTokenizer.phraseChunk(sentences(0))
    Console.println("Phrase Chunk :: Stanford")
    chunks.foreach(Console.println(_))
    val pvchunk = chunks.filter(chunk => "Pierre Vinken".equals(chunk._1))
    Assert.assertEquals(1, pvchunk.size)
    Assert.assertEquals("NP", pvchunk.head._2)
  }
  
  @Test 
  def testPosTagOpenNLP(): Unit = {
    // Open NLP
    val sentences = openNlpTokenizer.sentTokenize(text)
    val taggedWords = openNlpTokenizer.posTag(sentences(0))
    Console.println("POS Tag :: OpenNLP")
    taggedWords.foreach(Console.println(_))
    Assert.assertEquals("Pierre", taggedWords(0)._1)
    Assert.assertEquals("NNP", taggedWords(0)._2)
  }
  
  @Test
  def testPosTagLingPipe(): Unit = {
    // LingPipe
    val sentences = openNlpTokenizer.sentTokenize(text)
    val taggedWords = openNlpTokenizer.posTag(sentences(0))
    Console.println("POS Tag :: LingPipe")
    taggedWords.foreach(Console.println(_))
    Assert.assertEquals("Pierre", taggedWords(0)._1)
    Assert.assertEquals("NNP", taggedWords(0)._2)
  }
  
  @Test
  def testPosTagStanford(): Unit = {
    // Stanford
    val sentences = stanfordTokenizer.sentTokenize(text)
    val taggedWords = stanfordTokenizer.posTag(sentences(0))
    Console.println("POS Tag :: Stanford")
    taggedWords.foreach(Console.println(_))
    Assert.assertEquals("Pierre", taggedWords(0)._1)
    Assert.assertEquals("NNP", taggedWords(0)._2)
  }
}
