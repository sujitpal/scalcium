package com.mycompany.scalcium.externalapis

import java.io.File
import scala.io.Source
import scala.collection.JavaConversions._
import com.likethecolor.alchemy.api.Client
import com.likethecolor.alchemy.api.call.RankedNamedEntitiesCall
import com.likethecolor.alchemy.api.call.`type`.CallTypeText
import com.likethecolor.alchemy.api.params.NamedEntityParams
import com.likethecolor.alchemy.api.entity.NamedEntityAlchemyEntity
import com.likethecolor.alchemy.api.call.RankedConceptsCall
import com.likethecolor.alchemy.api.entity.ConceptAlchemyEntity
import com.likethecolor.alchemy.api.call.SentimentCall
import com.likethecolor.alchemy.api.entity.SentimentAlchemyEntity

class AlchemyMapper {

  val MyApiKey = "4e88eabb9f9547e9a8a75227e7919f499e847c67"
  val client = new Client(MyApiKey)
  
  def entities(file: File): List[NamedEntityAlchemyEntity] = {
    val text = toText(file)
    val params = new NamedEntityParams()
    params.setIsCoreference(true)
    params.setIsDisambiguate(true)
    params.setIsLinkedData(true)
    params.setIsQuotations(true)
    params.setIsSentiment(true)
    params.setIsShowSourceText(true)
    val theCall = new RankedNamedEntitiesCall(new CallTypeText(text), params)
    val resp = client.call(theCall)
    resp.iterator.toList
  }
  
  def topics(file: File): List[ConceptAlchemyEntity] = {
    val text = toText(file)
    val theCall = new RankedConceptsCall(new CallTypeText(text))
    val resp = client.call(theCall)
    resp.iterator.toList
  }
  
  def sentiments(file: File): List[SentimentAlchemyEntity] = {
    val text = toText(file)
    val theCall = new SentimentCall(new CallTypeText(text))
    val resp = client.call(theCall)
    resp.iterator.toList
  }
  
  def toText(file: File): String = {
    val source = Source.fromFile(file)
    val text = source.mkString
    source.close
    text
  }
}