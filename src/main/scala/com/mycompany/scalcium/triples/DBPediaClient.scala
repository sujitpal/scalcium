package com.mycompany.scalcium.triples

import scala.collection.mutable.ArrayBuffer

import com.hp.hpl.jena.query.Query
import com.hp.hpl.jena.query.QueryExecution
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory
import com.hp.hpl.jena.rdf.model.Literal

class DBPediaClient(url: String = "http://dbpedia.org/sparql") {

  val sparqlQueryTemplate = """
    PREFIX dbpedia: <http://dbpedia.org/resource/>
    PREFIX onto: <http://dbpedia.org/ontology/>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    
    SELECT ?label  WHERE {
      dbpedia:%s onto:%s ?o .
      ?o rdfs:label ?label .
    } LIMIT 100
  """

  def getObject(subj: String, verb: String): String = {
    val sparqlQuery = sparqlQueryTemplate
      .format(subj.replace(" ", "_"), verb)
    val query: Query = QueryFactory.create(sparqlQuery)
    val qexec: QueryExecution = QueryExecutionFactory.sparqlService(url, query)
    val results = qexec.execSelect()
    val objs = ArrayBuffer[String]()
    while (results.hasNext()) {
      val qsol = results.next()
      val literal = qsol.get("label").asInstanceOf[Literal]
      if ("en".equals(literal.getLanguage())) 
        objs += literal.getLexicalForm()
    }
    if (objs.isEmpty) null else objs.head
  }
}