package com.healthline.scalcium.freq

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.mapAsJavaMap
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.solr.common.params.MapSolrParams
import scala.io.Source
import com.healthline.query.kb.HealthConcept
import com.healthline.util.Config
import com.healthline.query.QueryEngine
import com.healthline.util.db.DBConnectionManager
import scala.collection.mutable.ArrayBuffer
import com.healthline.util.db.JDBCUtil
import java.sql.ResultSet
import java.sql.PreparedStatement

object ContentFreq extends App {

  val TestDataSize = 1000
  val PageSize = 10
  val InFile = new File("/tmp/hlcms_imuidsp_xx.txt")
  val OutFile = new File("/tmp/hlcms_imuids_0.txt")
  
  val ExcludedStyGroups = Set(
    "Environment Geographical location",
    "Event",
    "Healthcare Tools",
    "Health Plan",
    "NO Group",
    "NO GROUP",
    "NONE",
    "Qualifier",
    "SNOMED; to be determined",
    "Social Context"
  )
  val GetParentSql = """
    select concept_id 
    from concept_rel  
  	where rela_id = 1 
    and rconcept_id = ?
  """

  Config.setConfigDir("/prod/web/config")
  val server = new CommonsHttpSolrServer("http://sfc-solr01-prod:8080/solr/select")
  val qpe = QueryEngine.getQueryService()
  val conn = DBConnectionManager.getConnection()
  
//  retrieve("sourcename:hlcms", "imuids_p", InFile)
//  freqDist(InFile, OutFile)
  
  // first iteration
//  replaceWithParent(new File("/tmp/hlcms_imuids_0.txt"), new File("/tmp/hlcms_imuids_1.tmp"))
//  aggregate(new File("/tmp/hlcms_imuids_1.tmp"), new File("/tmp/hlcms_imuids_1.txt"))

  findKnee()
  
  conn.close();
  
  
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
          val imuidsp = result.getFieldValue("imuids_p").toString
          writer.println(imuidsp)
        } catch {
          case e: Throwable => { /* NOOP: just discard crap records */ }
        }
      })
    })
    Console.println("Processing complete")
    writer.flush()
    writer.close()
  }

  def freqDist(infile: File, outfile: File): Unit = {
    val writer = new PrintWriter(new FileWriter(outfile), true)
    val fd = scala.collection.mutable.Map[String,Int]()
    var ln = 0
    Source.fromFile(infile).getLines().
      foreach(line => {
        if (ln % 100 == 0) Console.println("Processing line %d".format(ln))
        val pairs = line.split(" ").
          map(pair => pair.split("\\$")(0)).
          filter(imuid => isAcceptableStyGroup(imuid)).
          map(imuid => {
            val count = fd.getOrElse(imuid, 0)
            fd(imuid) = count + 1
        })
        ln = ln + 1
    })
    Console.println("Processing complete")
    fd.map(kv => (kv._1, kv._2)).
      toList.
      sortWith((a, b) => a._2 > b._2).
      foreach(kv => writer.println("%s\t%d".format(kv._1, kv._2)))
    writer.flush()
    writer.close()
  }
  
  def isAcceptableStyGroup(imuid: String): Boolean = {
    val copt = Option[HealthConcept](qpe.getHealthConcept(imuid))
    copt match {
      case Some(concept) => {
        if (ExcludedStyGroups.contains(concept.getStyGroup())) false
        else true
      }
      case None => false
    }
  }

  def replaceWithParent(infile: File, outfile: File): Unit = {
    val writer = new PrintWriter(new FileWriter(outfile), true)
    Source.fromFile(infile).getLines().
      foreach(line => {
        val Array(imuid, count) = line.split("\t")
        val parents = getParents(imuid)
        if (parents.size == 0) writer.println(line)
        else parents.foreach(parent => 
          writer.println("%s\t%d".format(parent, count.toInt)))
    })
    writer.flush()
    writer.close()
  }
  
  def aggregate(infile: File, outfile: File): Unit = {
    val fd = scala.collection.mutable.Map[String,Int]()
    Source.fromFile(infile).getLines().foreach(line => {
      val Array(imuid, count) = line.split("\t")
      val currCount = fd.getOrElse(imuid, 0)
      fd(imuid) = currCount + count.toInt
    })
    val writer = new PrintWriter(new FileWriter(outfile), true)
    val parentCounts = fd.map(kv => (kv._1, kv._2.toInt)).
      toList.
      sortWith((a,b) => a._2 > b._2)
    parentCounts.foreach(kv => 
      writer.println("%s\t%d".format(kv._1, kv._2)))
    writer.flush()
    writer.close()
  }
  
  def getParents(imuid: String): List[String] = {
    val parents = new ArrayBuffer[String]()
    var ps: PreparedStatement = null
    var rs: ResultSet = null
    try {
      ps = conn.prepareStatement(GetParentSql)
      ps.setString(1, imuid)
      rs = ps.executeQuery()
      while (rs.next()) {
        parents += rs.getString(1)
      }
    } finally {
      if (rs != null) rs.close()
      if (ps != null) ps.close()
    }
    parents.toList
  }
  
  def findKnee(): Unit = {
    val counts = Source.fromFile(new File("/tmp/hlcms_imuids_0.txt")).
      getLines.
      toList.
      map(line => line.split("\t")(1).toInt).
      sliding(3).
      toList.
      map(l => l(0) + l(2) - (2 * l(1))).
      zipWithIndex
    val x = counts.reverse.filter(x => x._1 < 0)(0)._2
    Console.println(x)
  }
}