package com.healthline.scalcium.transformers

import java.io.File

import scala.io.Source

import org.junit.Test

class NegexAnnotatorTest {

  val dir = new File("src/main/resources/negex")
  val input = new File(dir, "test_input.txt")
  
  @Test
  def detailReport(): Unit = {
    val negAnnotator = new NegexAnnotator(
      new File(dir, "negex_triggers.txt"),
      List("Negated", "Affirmed"))
    val histAnnotator = new NegexAnnotator(
      new File(dir, "history_triggers.txt"),
      List("Historical", "Recent"))
    val hypAnnotator = new NegexAnnotator(
      new File(dir, "hypothetical_triggers.txt"),
      List("Not particular", "Recent"))
    val expAnnotator = new NegexAnnotator(
      new File(dir, "experiencer_triggers.txt"),
      List("Other", "Patient"))
    var numTested = 0
    var numCorrectNeg = 0
    var numCorrectHist = 0
    var numCorrectHyp = 0
    var numCorrectExp = 0
    Source.fromFile(input)
        .getLines()
        .foreach(line => {
      val cols = line.split("\t")
      val phrase = cols(2)
      val sentence = cols(3)
      val negActual = cols(4)
      val histActual = cols(5)
      val hypActual = cols(5)
      val expActual = cols(6)
      val negPred = negAnnotator.predict(sentence, phrase, false)
      val negCorrect = negActual.equals(negPred)
      val histPred = histAnnotator.predict(sentence, phrase, false)
      val histCorrect = histActual.equals(histPred) 
      val hypPred = hypAnnotator.predict(sentence, phrase, false)
      val hypCorrect = hypActual.equals(hypPred)
      val expPred = expAnnotator.predict(sentence, phrase, false)
      val expCorrect = expActual.equals(expPred)
      numCorrectNeg += (if (negCorrect) 1 else 0)
      numCorrectHist += (if (histCorrect) 1 else 0)
      numCorrectHyp += (if (hypCorrect)1 else 0)
      numCorrectExp += (if (expCorrect) 1 else 0)
      numTested += 1
      Console.println("%s\t%s\t[%s] %s\t[%s] %s\t[%s] %s\t[%s] %s"
        .format(phrase, sentence, 
          if (negCorrect) "+" else "-", negActual,
          if (histCorrect) "+" else "-", histActual,
          if (hypCorrect) "+" else "-", hypActual,
          if (expCorrect) "+" else "-", expActual))
    })
    Console.println()
    Console.println("Accuracy Scores")
    Console.println("Accuracy (Negation) = %6.4f"
      .format(numCorrectNeg.toDouble / numTested.toDouble))
    Console.println("Accuracy (Historical) = %6.4f"
      .format(numCorrectHist.toDouble / numTested.toDouble))
    Console.println("Accuracy (Hypothetical) = %6.4f"
      .format(numCorrectHyp.toDouble / numTested.toDouble))
    Console.println("Accuracy (Experiencer) = %8.6f"
      .format(numCorrectExp.toDouble / numTested.toDouble))
  }
}