package com.mycompany.scalcium.utils

import java.io.File
import mx.bigdata.jcalais.rest.CalaisRestClient
import scala.io.Source
import mx.bigdata.jcalais.CalaisResponse
import mx.bigdata.jcalais.CalaisObject
import scala.collection.JavaConversions._

class OpenCalaisMapper {

  val MyApiKey = "zcq3x5atkurskvxrak782v4r"
  val client = new CalaisRestClient(MyApiKey)
  
  
  def map(file: File): CalaisResponse = {
    val source = Source.fromFile(file)
    val text = source.mkString
    source.close
    client.analyze(text)
  }
  
  def entities(resp: CalaisResponse): List[CalaisObject] =
    resp.getEntities().toList
    
  def topics(resp: CalaisResponse): List[CalaisObject] = 
    resp.getTopics().toList
    
  def socialTags(resp: CalaisResponse): List[CalaisObject] = 
    resp.getSocialTags().toList
    
  def relations(resp: CalaisResponse): List[CalaisObject] = 
    resp.getRelations().toList
}