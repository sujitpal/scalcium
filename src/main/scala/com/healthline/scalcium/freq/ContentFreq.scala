package com.healthline.scalcium.freq

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.sql.PreparedStatement
import java.sql.ResultSet

import scala.Array.canBuildFrom
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.mapAsJavaMap
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.solr.common.params.MapSolrParams

import com.healthline.query.QueryEngine
import com.healthline.query.kb.HealthConcept
import com.healthline.util.Config
import com.healthline.util.db.DBConnectionManager

object ContentFreq extends App {

  val TestDataSize = 1000
  val PageSize = 10
  val TopN = 10
  val InFile = new File("/tmp/hlcms_imuidsp_xx.txt")
  val InFile0 = new File("/tmp/hlcms_imuids_0.txt")
  val InFile1 = new File("/tmp/hlcms_imuids_1.txt")
  val InFile2 = new File("/tmp/hlcms_imuids_2.txt")
  val InFile3 = new File("/tmp/hlcms_imuids_3.txt")
  val InFile4 = new File("/tmp/hlcms_imuids_4.txt")
  val InFile5 = new File("/tmp/hlcms_imuids_5.txt")
  val InFile6 = new File("/tmp/hlcms_imuids_6.txt")
  val InFile7 = new File("/tmp/hlcms_imuids_7.txt")
  val InFile8 = new File("/tmp/hlcms_imuids_8.txt")
  val InFile9 = new File("/tmp/hlcms_imuids_9.txt")
  val InFile10 = new File("/tmp/hlcms_imuids_10.txt")
  val InFile11 = new File("/tmp/hlcms_imuids_11.txt")
  
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
//  freqDist(InFile, InFile0)
//  showTop10(InFile0)

  // iterations
//  rollup(InFile0, InFile1)
//  rollup(InFile1, InFile2)
//  rollup(InFile2, InFile3)
//  rollup(InFile3, InFile4)
//  rollup(InFile4, InFile5)
//  rollup(InFile5, InFile6)
//  rollup(InFile6, InFile7)
//  rollup(InFile7, InFile8)
  rollup(InFile8, InFile9)
  
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

  def showTop10(infile: File): Unit = {
    var ln = 0
    Source.fromFile(infile).getLines().
      foreach(line => {
        if (ln < TopN) {
          val Array(imuid, count) = line.split("\t")
          Console.println("%s (%s): %d".format(imuid, getCFN(imuid), count.toInt))
          ln = ln + 1
        }
    })  
  }
  
  def rollup(infile: File, outfile: File): Unit = {
    val Temp1 = new File("/tmp/rollup_1.tmp")
    val Temp2 = new File("/tmp/rollup_2.tmp")
    // write out parents of current IMUIDs
    val writer = new PrintWriter(new FileWriter(Temp1), true)
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
    // aggregate the IMUIDs 
    val fd = scala.collection.mutable.Map[String,Int]()
    Source.fromFile(Temp1).
      getLines().
      foreach(line => {
        val Array(imuid, count) = line.split("\t")
        val currCount = fd.getOrElse(imuid, 0)
        fd(imuid) = currCount + count.toInt
    })
    val writer2 = new PrintWriter(new FileWriter(Temp2), true)
    val parentCounts = fd.map(kv => (kv._1, kv._2.toInt)).
      toList.
      sortWith((a,b) => a._2 > b._2)
    parentCounts.foreach(kv => 
      writer2.println("%s\t%d".format(kv._1, kv._2)))
    writer2.flush()
    writer2.close()
    // based on Pareto principle, cutoff at 80% of sum
    val counts = fd.map(kv => kv._2.toInt).toList
    val sum = counts.foldLeft(0)(_ + _)
    val cutoff = sum.toDouble * 0.8D
    val cutoffIdx = counts.scanLeft(0)(_ + _).tail.
      zipWithIndex.
      filter(_._1 < cutoff).
      size
    Console.println("#-rows=" + counts.size + ", cutoff at=" + 
      cutoffIdx + "(" + cutoff + ")")
    // truncate and write into outfile
    val writer3 = new PrintWriter(new FileWriter(outfile), true)
    var ln = 0
    val x = Source.fromFile(Temp2).
      getLines().
      foreach(line => {
        if (ln < cutoffIdx) {
          val Array(imuid, count) = line.split("\t")
          val score = 100.0D * count.toDouble / sum.toDouble
          if (ln < TopN) Console.println("%s (%s): %d, %f%%".format(
            getCFN(imuid), imuid, count.toInt, score))
          writer3.println(line)
        }
        ln = ln + 1
    })
    writer3.flush()
    writer3.close()
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
  
  def getCFN(imuid: String): String = {
    val copt = Option[HealthConcept](qpe.getHealthConcept(imuid))
    copt match {
      case Some(concept) => concept.getCFN()
      case None => imuid
    }
  }
}