package com.healthline.scalcium

import org.junit.Test
import org.junit.Assert

class NegationHandlerTest {
  
  val tests = Array[String](
    "Bone scans done in the past showed no signs of metastases.",
    "No new evidence was found following a chest X-ray.",
    "Patient had no family history of diabetes.",
    "While depressed, patient shows no sign of dementia."
  )
  val tokenizer = new Tokenizer()
  
  @Test def testMaskNegative(): Unit = {
    tests.foreach(sentence => {
      Console.println("INPUT: " + sentence)
      val osentence = NegationHandler.maskNegative(sentence, tokenizer)
      Console.println("OUTPUT: " + osentence)
      Assert.assertTrue(osentence.indexOf(NegationHandler.NegationPrefix) > -1)
    })
  }
}