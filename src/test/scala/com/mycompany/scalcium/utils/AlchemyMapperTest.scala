package com.mycompany.scalcium.utils

import org.junit.Test
import java.io.File
import org.junit.Assert

class AlchemyMapperTest {

  val InputFile = "/tmp/input.txt"

  @Test
  def testAnalyzeFile(): Unit = {
    val mapper = new AlchemyMapper()
    Console.println("== Entities ==")
    val entities = mapper.entities(new File(InputFile))
    entities.foreach(entity => Console.println("%s/%s (%5.3f)".format(
      entity.getText(), entity.getType(), entity.getScore())))
    Assert.assertTrue(entities.size > 0)
    Console.println("== Topics ==")
    val topics = mapper.topics(new File(InputFile))
    topics.foreach(topic => Console.println("%s (%5.3f)".format(
      topic.getConcept(), topic.getScore())))
    Assert.assertTrue(topics.size > 0)
    Console.println("== Sentiments ==")
    val sentiments = mapper.sentiments(new File(InputFile))
    sentiments.foreach(sentiment => Console.println(
      "Mixed: %s %s (%5.2f)".format(sentiment.isMixed(), 
      sentiment.getType(), sentiment.getScore())))
    Assert.assertTrue(sentiments.size > 0)
  }
}