package com.healthline.scalcium.transformers

import java.io.File

import scala.io.Source

import org.junit.Ignore
import org.junit.Test

class TemporalAnnotatorTest {

  val tann = new TemporalAnnotator(
    new File("src/main/resources/TempoWnL_1.0.txt"))
  val input = new File("src/main/resources/negex/test_input.txt")

  @Test
  @Ignore
  def testAlgoWithSingleSentence(): Unit = {
    val s = "Recent Social History: Former Tobacco"
    val predicted = tann.predict(s)
    Console.println("predicted=" + predicted)
  }
  
  @Test
  def evaluate(): Unit = {
    var numTested = 0
    var numCorrect = 0
    Source.fromFile(input).getLines().foreach(line => {
      val cols = line.split("\t")
      val sentence = cols(3)
      val actual = cols(5)
      if ((! "Not particular".equals(actual))) {
        val predicted = tann.predict(sentence)
        val correct = actual.equals(translate(predicted))
        if (! correct) {
          Console.println("%s|[%s] %s|%s"
            .format(sentence, (if (correct) "+" else "-"), 
              actual, predicted))
        }
        numCorrect += (if (correct) 1 else 0)
        numTested += 1
      }
    })
    Console.println("Accuracy=%8.6f"
      .format(numCorrect.toDouble / numTested.toDouble))
  }
  
  /**
   * Converts predictions made by TemporalAnnotator to
   * predictions that match annotations in our testcase.
   */
  def translate(pred: String): String = {
    pred match {
      case "Past" => "Historical"
      case _ => "Recent"
    }
  }
}