package com.healthline.scalcium.umls

import org.neo4j.rest.graphdb.RestAPIFacade
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine

import spray.json._
import DefaultJsonProtocol._

import scala.collection.JavaConversions._

class NeoClient {

  val db = new RestAPIFacade("http://localhost:7474/db/data")
  val engine = new RestCypherQueryEngine(db)
  
  //////////////////// services ///////////////////////////
  
  def getConceptByCui(cui: String): Option[Concept] = {
    val result = engine.query("""
      start n=node:concepts(cui={cui}) match (n) 
      return n.cui, n.syns, n.stys""",
      Map(("cui", cui)))
    val iter = result.iterator()
    if (iter.hasNext()) {
      val row = iter.next()
      val cui = row("n.cui").asInstanceOf[String]
      val syns = row("n.syns").asInstanceOf[String]
          .replaceAll("'", "\"")
          .asJson
          .convertTo[List[String]]
      val stys = row("n.stys").asInstanceOf[String]
          .replaceAll("'", "\"")
          .asJson
          .convertTo[List[String]]
      Some(Concept(cui, syns, stys))
    } else None  
  }
  
  def listRelationships(cui: String): List[String] = {
    val result = engine.query("""
      start m=node:concepts(cui={cui}) 
      match (m)-[r]->(n)
      return distinct type(r) as rel""", 
      Map(("cui", cui)))
    result.iterator()
      .map(row => row("rel").asInstanceOf[String])
      .toList
  }
  
  def listRelatedConcepts(cui: String, rel: String): 
      List[String] = {
    val result = engine.query("""
      start m=node:concepts(cui={cui})
      match (m)-[%s]->(n)
      return n.cui""".format(rel),
      Map(("cui", cui)))
    result.iterator()
      .map(row => row("n.cui").asInstanceOf[String])
      .toList
  }
  
  def shortestPath(cui1: String, cui2: String, 
      maxLen: Int): (List[String], List[String], Int) = {
    val result = engine.query("""
      start m=node:concepts(cui={cui1}), 
        n=node:concepts(cui={cui2})
      match p = shortestPath(m-[*..%d]->n)
      return p""".format(maxLen), 
      Map(("cui1", cui1), ("cui2", cui2)))
    val iter = result.iterator()
    if (iter.hasNext()) {
      val row = iter.next()
      row("p") match {
        case (nvps: java.util.LinkedHashMap[String,_]) => {
          val nodelist = nvps("nodes") match {
            case (nodes: java.util.ArrayList[_]) => 
              nodes.map(url => 
                getCuiByUrl(url.asInstanceOf[String]))
              .toList
              .flatten
            case _ => List()
          }
          val rellist = nvps("relationships") match {
            case (rels: java.util.ArrayList[_]) => 
              rels.map(url => 
                getRelNameByUrl(url.asInstanceOf[String]))
              .toList
              .flatten
            case _ => List()
          }
          val length = nvps("length").asInstanceOf[Int]
          return (nodelist, rellist, length)          
        }
      }
    }
    return (List(), List(), -1)
  }

  //////////////// methods for internal use //////////////////
  
  def getConceptById(id: Int): Option[Concept] = {
    getCuiById(id) match {
      case Some(cui: String) => getConceptByCui(cui)
      case None => None
    }
  }
  
  def getCuiByUrl(url: String): Option[String] = 
    getCuiById(getIdFromUrl(url))
  
  def getCuiById(id: Int): Option[String] = {
    val result = engine.query("""
      start n=node(%d) return n.cui limit 1"""
      .format(id), Map[String,Object]())
    val iter = result.iterator()
    if (iter.hasNext()) {
      val row = iter.next()
      Some(row("n.cui").asInstanceOf[String])
    } else None
  }
  
  def getRelNameByUrl(url: String): Option[String] =
    getRelNameById(getIdFromUrl(url))
  
  def getRelNameById(id: Int): Option[String] = {
    val result = engine.query("""
      start r=relationship(%d) return type(r) as rt limit 1"""
      .format(id), Map[String,Object]())
    val iter = result.iterator()
    if (iter.hasNext()) {
      val row = iter.next()
      Some(row("rt").asInstanceOf[String])
    } else None
  }
  
  def getIdFromUrl(url: String): Int = 
    url.substring(url.lastIndexOf('/') + 1).toInt
}

case class Concept(val cui: String, 
                   val syns: List[String], 
                   val stys: List[String])