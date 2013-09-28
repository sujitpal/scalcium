package com.healthline.scalcium.freq

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

import scala.Array.canBuildFrom
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.mapAsJavaMap
import scala.collection.TraversableOnce.flattenTraversableOnce
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.solr.common.params.MapSolrParams

import com.healthline.query.QueryEngine
import com.healthline.query.QueryService
import com.healthline.query.kb.HealthConcept
import com.healthline.util.Config
import com.healthline.util.db.DBConnectionManager

object TopicFreq extends App {

  val InFile = new File("/tmp/topicfreq_0.txt")
  val OutFileRef = new File("/tmp/topicfreq_ref.txt")
  val OutFile1 = new File("/tmp/topicfreq_1.txt")
  val OutFile2 = new File("/tmp/topicfreq_2.txt")
  val OutFile3 = new File("/tmp/topicfreq_3.txt")
  
  val PageSize = 10

  Config.setConfigDir("/prod/web/config")
  val qpe = QueryEngine.getQueryService()
  val conn = DBConnectionManager.getConnection()
  val server = new CommonsHttpSolrServer("http://sfc-solr01-prod:8080/solr/select")

  /////////////// get mapping of related concepts to concepts list /////////////
  val imuids = getTopicImuids(conn)
//  val rollupMap = buildRollupMap(imuids)

  ///////////////// retrieve data from solr for hl_cms ////////////////////////
//  retrieve("sourcename:hlcms", "itemtitle,imuids_p,url", InFile)
//  splitCols(InFile, OutFileRef, OutFile1)

  ///////////////// rollup concepts and aggregate /////////////////////////////
//  rollup(rollupMap, OutFile1, OutFile2)
  
  ///////////////// calculate parents for graph ///////////////////////////////
  rollupParents(OutFile2, OutFile3)
  
  conn.close()
  
  //////////////////////////////////////////////////////////////////////////////////
  
  def getTopicImuids(conn: Connection): List[String] = 
    _getRelatedImuids(conn, "select imuid from topics_map", null)
  
  def getRelatedImuids(conn: Connection, imuid: String): List[String] =
    _getRelatedImuids(conn, """
        select rconcept_id from concept_rel
        where rela_id in (1,4,5,10)
        and concept_rank >= 5
        and concept_id = ?""", imuid)
  
  def getParentImuids(conn: Connection, imuid: String): List[String] = 
    _getRelatedImuids(conn, """
        select concept_id from concept_rel 
        where rela_id = 1 and rconcept_id = ?""", imuid)

  def buildRollupMap(imuids: List[String]): Map[String,String] = {
	imuids.filter(imuid => isDisease(qpe, imuid))
          .map(imuid => getRelatedImuids(conn, imuid)
                        .map((rimuid => (rimuid, imuid))))
          .flatten
          .groupBy(kv => kv._1)         // group by related concept
          .filter(kv => kv._2.size > 1) // discard if related concept 
                                        // points back to >1 disease
          .map(kv => kv._2)             // remove key values are list(nvp)
          .flatten
          .toMap
  } 
  
  def getCFN(qpe: QueryService, imuid: String): String = {
    _getConceptAttribute(qpe, imuid, _getCFN)  
  }
  
  def isDisease(qpe: QueryService, imuid: String): Boolean = {
    val stygroup = _getConceptAttribute(qpe, imuid, _getStyGroup)
    "diseases".equals(stygroup)
  }

  def retrieve(q: String, fl: String, outfile: File): Unit = {
    val nparams = new MapSolrParams(Map(
      ("q", q), 
      ("rows", 0.toString()), 
      ("fl", fl)))
    val nrsp = server.query(nparams)
    val numFound = nrsp.getResults().getNumFound() 
    val npages = ((numFound / PageSize) + 
      (if (numFound % PageSize == 0) 0 else 1)).toInt
    Console.println("numfound: %d, numpages: %d".format(numFound, npages))
    val writer = new PrintWriter(new FileWriter(outfile), true)
    (0 until npages).foreach(page => {
      if (page % 100 == 0) Console.println("Processing page: %d".format(page))
      val start = page * PageSize
      val dparams = new MapSolrParams(Map(
        ("q", q), 
        ("start", start.toString), 
        ("rows", PageSize.toString()), 
        ("fl", fl)))
      val drsp = server.query(dparams)
      drsp.getResults().foreach(result => {
        try {
          val itemtitle = result.getFieldValue("itemtitle").toString
          val url = result.getFieldValue("url").toString
          val imuidsp = result.getFieldValue("imuids_p").toString
          writer.println("%s\t%s\t%s".format(itemtitle, url, imuidsp))
        } catch {
          case e: Throwable => { /* NOOP: just discard crap records */ }
        }
      })
    })
    Console.println("Processing complete")
    writer.flush()
    writer.close()
  }
  
  def splitCols(infile: File, refFile: File, outfile: File): Unit = {
    val refwriter = new PrintWriter(new FileWriter(refFile), true)
    val outwriter = new PrintWriter(new FileWriter(outfile), true)
    Source.fromFile(infile)
          .getLines()
          .foreach(line => {
            val Array(title, url, imuidsp) = line.split("\t")
            refwriter.println("%s\t%s".format(title, url))
            val imuids = imuidsp.split(" ")
                                .map(idscore => idscore.split("\\$")(0))
                                .mkString(" ")
            outwriter.println(imuids)
          })
    refwriter.flush()
    refwriter.close()
    outwriter.flush()
    outwriter.close()
  }
  
  def rollup(mapping: Map[String,String], infile: File, outfile: File): Unit = {
    val rollup = Source.fromFile(infile)
                       .getLines()
                       .map(line => line.split(" "))
                       .flatten
                       .map(imuid => mapping.getOrElse(imuid, "NOTFOUND"))
                       .filter(imuid => (! "NOTFOUND".equals(imuid)))
                       .toList
                       .groupBy(imuid => imuid)
                       .map(kv => (kv._1, kv._2.size))
                       .toList
                       .sortWith((a,b) => a._2 > b._2)
    val sum = rollup.map(kv => kv._2)
                    .foldLeft(0)(_ + _)
    val writer = new PrintWriter(new FileWriter(outfile), true)
    rollup.foreach(kv => writer.println(
      "%s (%s): %-5.2f".format(getCFN(qpe, kv._1), kv._1, kv._2.toDouble * 100 / sum)))
    writer.flush()
    writer.close()
  }

  def rollupParents(infile: File, outfile: File): Unit = {
    val parents = ArrayBuffer[(String,Double)]()
    Source.fromFile(infile)
          .getLines()
          .foreach(line => {
             val Array(cfn, imuid, pcount) = line.replaceAll("\\s\\(", "\t")
                             .replaceAll("\\):\\s", "\t")
                             .split("\t")
             val parentImuids = getParentImuids(conn, imuid)
             parentImuids.foreach(pimuid => parents += ((pimuid, pcount.toDouble)))
    })
    val parentScores = parents.groupBy(kv => kv._1)
                   .map(kv => (kv._1, kv._2.map(
                       tuple => tuple._2).foldLeft(0.0)(_ + _)))
                   .toList
                   .sortWith((a, b) => a._2 > b._2)
    val sum = parentScores.map(kv => kv._2).foldLeft(0.0D)(_ + _)               
    val writer = new PrintWriter(new FileWriter(outfile), true)
    parentScores.foreach(kv => writer.println(
      "%s (%s): %f".format(getCFN(qpe, kv._1), kv._1, 100.0D * kv._2 / sum)))
    writer.flush()
    writer.close()
  }
  
  ///////////////////// (private methods) ///////////////////////////

  def _getRelatedImuids(conn: Connection, sql: String, 
      imuid: String): List[String] = {
    val relatedImuids = ArrayBuffer[String]()
    var ps: PreparedStatement = null
    var rs: ResultSet = null
    try {
      ps = conn.prepareStatement(sql)
      if (imuid != null) ps.setString(1, imuid)
      rs = ps.executeQuery()
      while (rs.next()) {
        relatedImuids += rs.getString(1)
      }
    } catch {
      case e: SQLException => e.printStackTrace()
    } finally {
      if (rs != null) rs.close()
      if (ps != null) ps.close()
    }
    relatedImuids.toList
  }
  
  def _getConceptAttribute(qpe: QueryService, imuid: String, 
      f: HealthConcept => String): String = {
    val copt = Option[HealthConcept](qpe.getHealthConcept(imuid))
    copt match {
      case Some(concept) => f.apply(concept)
      case None => null
    }
  }
  
  def _getCFN(concept: HealthConcept): String = concept.getCFN()
  
  def _getStyGroup(concept: HealthConcept): String = concept.getStyGroup()
}
