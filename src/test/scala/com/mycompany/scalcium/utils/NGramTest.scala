package com.mycompany.scalcium.utils

import org.junit.{Assert, Test}
import com.mycompany.scalcium.tokenizers.Tokenizer

class NGramTest {

  val sentence = "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29."
  val tokenizer = Tokenizer.getTokenizer("opennlp")
  val words = tokenizer.wordTokenize(sentence)
  
  @Test def testBigrams(): Unit = {
    val bigrams = NGram.bigrams(words)
    Console.println("== bigrams ==")
    bigrams.foreach(Console.println(_))
    Assert.assertEquals(bigrams(0)(0), "Pierre")
    Assert.assertEquals(bigrams(0)(1), "Vinken")
  }
  
  @Test def testTrigrams() : Unit = {
    val trigrams = NGram.trigrams(words)
    Console.println("== trigrams ==")
    trigrams.foreach(Console.println(_))
    Assert.assertEquals(trigrams(0)(0), "Pierre")
    Assert.assertEquals(trigrams(0)(1), "Vinken")
    Assert.assertEquals(trigrams(0)(2), ",")
  }
  
  @Test def testNGrams(): Unit = {
    val quadgrams = NGram.ngrams(words, 4)
    Console.println("== quadgrams ==")
    quadgrams.foreach(Console.println(_))
    Assert.assertEquals(quadgrams(0)(0), "Pierre")
    Assert.assertEquals(quadgrams(0)(1), "Vinken")
    Assert.assertEquals(quadgrams(0)(2), ",")
    Assert.assertEquals(quadgrams(0)(3), "61")    
  }
}
