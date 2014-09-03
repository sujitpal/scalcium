package com.mycompany.scalcium.externalapis

import org.junit.Test
import java.io.File
import scala.collection.JavaConversions._
import org.junit.Assert
import com.mycompany.scalcium.externalapis.OpenCalaisMapper

class OpenCalaisMapperTest {

  val InputFile = "/tmp/input.txt"
    
  @Test
  def testAnalyzeFile(): Unit = {
    val mapper = new OpenCalaisMapper()
    val resp = mapper.map(new File(InputFile))
    Console.println("== Entities ==")
    val entities = mapper.entities(resp)
    entities.foreach(entity => Console.println("%s/%s (%s)".format(
        entity.getField("name"), entity.getField("_type"), 
        entity.getField("relevance"))))
    Assert.assertTrue(entities.size > 0)
    Console.println("== Topics ==")
    val topics = mapper.topics(resp)
    topics.foreach(topic => Console.println("%s (%s)".format(
      topic.getField("categoryName"), topic.getField("score"))))
    Assert.assertTrue(topics.size > 0)
    Console.println("== Social Tags ==")
    val socialTags = mapper.socialTags(resp)
    socialTags.foreach(stag => Console.println("%s/%s".format(
      stag.getField("name"), stag.getField("_typeGroup"))))
    Assert.assertTrue(socialTags.size > 0)
  }
}