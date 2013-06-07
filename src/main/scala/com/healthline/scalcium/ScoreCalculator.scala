package com.healthline.scalcium

import com.healthline.query.QueryEngine
import com.healthline.query.MappedHealthConcept
import scala.collection.mutable.ArrayBuffer
import collection.JavaConversions._
import com.healthline.query.kb.HealthConcept

object ScoreCalculator {

  val qpe = QueryEngine.getQueryService()
  
  def baseScores(phrases: List[String]): Map[HealthConcept,Float] = {
    val concepts = ArrayBuffer[MappedHealthConcept]()
    phrases.foreach(phrase => {
      val omapper = Option(qpe.getHealthConceptMapperForQuery(phrase))
      omapper match {
        case Some(mapper) => {
          val omhcs = Option(mapper.getMappedHealthConcepts())
          omhcs match {
            case Some(mhcs) => concepts.addAll(mhcs)
            case None => ()
          }
        }
        case None => ()
      }
    })
    val conceptMap = scala.collection.mutable.Map[HealthConcept,Float]()
    concepts.foreach(concept => {
      if (conceptMap.contains(concept)) {
        val prevCount = conceptMap(concept)
        conceptMap(concept) = prevCount + 1.0F
      } else conceptMap(concept) = 1.0F
    })
    conceptMap.toMap
  }
}