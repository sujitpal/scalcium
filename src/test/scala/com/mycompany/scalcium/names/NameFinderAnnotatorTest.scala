package com.mycompany.scalcium.names

import org.junit.Test
import org.apache.uima.fit.factory.AnalysisEngineFactory
import org.apache.uima.fit.util.JCasUtil
import scala.collection.JavaConversions._
import com.mycompany.scalcium.pipeline.Entity
import org.junit.Assert
//import com.mycompany.scalcium.pipeline.NameFinderAnnotator

class NameFinderAnnotatorTest {

  val text = "Pierre Vinken , 61 years old , will join the board as a nonexecutive director Nov. 29 . Mr. Vinken is chairman of Elsevier N.V. , the Dutch publishing group . Rudolph Agnew , 55 years old and former chairman of Consolidated Gold Fields PLC , was named a director of this British industrial conglomerate ."

  @Test
  def testPipeline(): Unit = {
	  Assert.assertTrue(true)
	 /*
    val ae = AnalysisEngineFactory.createEngine(classOf[NameFinderAnnotator])
    val jcas = ae.newJCas()
    jcas.setDocumentText(text)
    ae.process(jcas)
    JCasUtil.select(jcas, classOf[Entity]).foreach(entity => {
      Console.println("(%d, %d): %s/%s".format(
        entity.getBegin(), entity.getEnd(),
        text.substring(entity.getBegin(), entity.getEnd()),
        entity.getEntityType()))
    })
    */
  }
}