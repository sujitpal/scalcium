package com.healthline.scalcium.transformers

import java.io.File
import scala.io.Source
import org.junit.Test
import org.junit.Ignore

class NegexAnnotatorTest {

  val dir = new File("src/main/resources/negex")
  val input = new File(dir, "test_input.txt")
  
  @Test
  @Ignore
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
    // for Historical/Hypothetical, there is only a single
    // annotation with (Historical/Recent/Not particular),
    // so we need some custom logic to determine accuracy
    // for this Historical and Hypothetical annotators
    var numTestedHist = 0
    var numTestedHyp = 0
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
      var histCorrect = false
      if (! "Not particular".equals(histActual)) {
        histCorrect = histActual.equals(histPred) 
        numCorrectHist += (if (histCorrect) 1 else 0)
        numTestedHist += 1
      }
      val hypPred = hypAnnotator.predict(sentence, phrase, false)
      var hypCorrect = false
      if (! "Historical".equals(hypActual)) {
        hypCorrect = hypActual.equals(hypPred)
        numCorrectHyp += (if (hypCorrect)1 else 0)
        numTestedHyp += 1
      }
      val expPred = expAnnotator.predict(sentence, phrase, false)
      val expCorrect = expActual.equals(expPred)
      numCorrectNeg += (if (negCorrect) 1 else 0)
      numCorrectExp += (if (expCorrect) 1 else 0)
      numTested += 1
      Console.println("%s|%s|[%s] %s|[%s] %s|[%s] %s|[%s] %s"
        .format(phrase, sentence, 
          if (negCorrect) "+" else "-", negActual,
          if (histCorrect) "+" else "-", histActual,
          if (hypCorrect) "+" else "-", hypActual,
          if (expCorrect) "+" else "-", expActual))
    })
    Console.println()
    Console.println("Accuracy Scores")
    Console.println("Accuracy (Negation) = %8.6f"
      .format(numCorrectNeg.toDouble / numTested.toDouble))
    Console.println("Accuracy (Historical) = %8.6f"
      .format(numCorrectHist.toDouble / numTestedHist.toDouble))
    Console.println("Accuracy (Hypothetical) = %8.6f"
      .format(numCorrectHyp.toDouble / numTestedHyp.toDouble))
    Console.println("Accuracy (Experiencer) = %8.6f"
      .format(numCorrectExp.toDouble / numTested.toDouble))
  }
  
  @Test
  def testExample(): Unit = {
    val inputs = List(
      ("Flu vaccine", "Member stated she does not receive Flu vaccine and hasnt for years, reviewed prevention techniques to help from getting the Flu."),
      ("the flu", "Member stated she does not receive Flu vaccine and hasnt for years, reviewed prevention techniques to help from getting the Flu."),
      ("Pneumonia vaccine", "She states she has had the Pneumonia vaccine but will check to see if she needs a booster now."),
      ("chicken pox", "She is unsure if she ever had chicken pox but says her sister remembers everything and she will ask her."),
      ("chicken pox", "Her sister had chicken pox but she escaped getting infected somehow."))  
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
    inputs.foreach(input => {
      val negPred = negAnnotator.predict(input._2, input._1, false)
      val histPred = histAnnotator.predict(input._2, input._1, false)
      val hypPred = hypAnnotator.predict(input._2, input._1, false)
      val expPred = expAnnotator.predict(input._2, input._1, false)
      Console.println("%s|%s|%s|%s|%s|%s".format(
        input._1, input._2, negPred, histPred, hypPred, expPred))
    })
  }
}
