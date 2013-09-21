package com.healthline.scalcium.tagger

import java.io.{File, FileWriter, PrintWriter}
import scala.io.Source
import org.apache.solr.common.params.MapSolrParams
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import com.healthline.util.Config
import com.healthline.query.QueryEngine
import com.healthline.query.kb.HealthConcept
import scala.collection.GenSet
import scala.collection.JavaConversions._
import org.apache.commons.lang3.StringUtils

object AutoTagger extends App {

  // arguments
  val InputData = new File("/tmp/hlcms.txt")
  val CleanInputData = new File("/tmp/hlcms2.txt")
  val ModelData = new File("/tmp/hlcms_model.txt")
  val TestData = new File("/tmp/hlcms_test.txt")
  val ReportData = new File("/tmp/hlcms_report.txt")
  val server = new CommonsHttpSolrServer("http://sfc-solr01-prod:8080/solr/select")

  // configurable constants
  val PageSize = 10
  val TestDataSize = 1000
  val RecoPercentCutoff = 5
  val RecoNumber = 3

  // constants
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
  val DomainMappings = Map(
    (137, "Ear, Nose, and Throat"),
    (136, "Disease and Injury Prevention "),
    (139, "Fitness and Exercise"),
    (138, "Eye Health"),
    (141, "Headaches"),
    (140, "Genetic Conditions"),
    (143, "Heart Conditions"),
    (142, "Healthy Eating and Nutrition"),
    (129, "Cancer"),
    (128, "Beauty and Skin Care"),
    (131, "Chronic Conditions"),
    (130, "Children's Health "),
    (133, "Diabetes"),
    (132, "COPD"),
    (135, "Digestive Health"),
    (134, "Diet and Weight Loss"),
    (152, "Smoking Cessation"),
    (153, "STDs and Sexual Health"),
    (154, "Stress and Mental Wellness"),
    (155, "Substance Abuse"),
    (156, "Urinary (including Kidneys)"),
    (157, "Women's Health "),
    (158, "Senior Health"),
    (144, "Immune Disorders"),
    (145, "Men's Health"),
    (146, "Mental Health"),
    (147, "Mind and Body"),
    (148, "Neurological Disorders (including stroke)"),
    (149, "Oral Health"),
    (150, "Pain Management"),
    (151, "Pregnancy and Childbirth "),
    (127, "Autoimmune Conditions"),
    (126, "Asthma"),
    (125, "Arthritis (Bones, Joints, Muscles)"),
    (124, "Allergies")
  )
  val DomainKeys = DomainMappings.keySet

  Config.setConfigDir("/prod/web/config")
  val qpe = QueryEngine.getQueryService()

//  retrieve("+domainids:[* TO *]", "sourcename:hlcms", 
//      "domainids,imuids_p", InputData, true)
//  removeBadSemanticGroups(InputData, CleanInputData) 
//  train(CleanInputData, ModelData)
//  retrieve("-domainids:[* TO *]", "sourcename:hlcms", 
//    "itemtitle,imuids_p", TestData, false)
  test(ModelData, TestData, ReportData)
  
    
  def retrieve(q: String, fq: String, fl: String, outfile: File, train: Boolean): Unit = {
    val nparams = new MapSolrParams(Map(
      ("q", q), 
      ("fq", fq), 
      ("rows", 0.toString()), 
      ("fl", fl)))
    val nrsp = server.query(nparams)
    val numFound = if (train) nrsp.getResults().getNumFound() 
      else TestDataSize
    val npages = ((numFound / PageSize) + 
      (if (numFound % PageSize == 0) 0 else 1)).toInt
    Console.println("numfound: %d, numpages: %d".format(numFound, npages))
    val writer = new PrintWriter(new FileWriter(outfile), true)
    (0 until npages).foreach(page => {
      if (page % 100 == 0) Console.println("Processing page: %d".format(page))
      val start = page * PageSize
      val dparams = new MapSolrParams(Map(
        ("q", q), 
        ("fq", fq), 
        ("start", start.toString), 
        ("rows", PageSize.toString()), 
        ("fl", fl)))
      val drsp = server.query(dparams)
      drsp.getResults().foreach(result => {
        try {
          if (train) {
            val domainIds = result.getFieldValues("domainids").
              map(_.toString).
              toList
            val imuidsp = result.getFieldValue("imuids_p").toString
            domainIds.foreach(domainId => 
              writer.println("%s\t%s".format(domainId, imuidsp)))
          } else {
        	val itemtitle = result.getFieldValue("itemtitle").toString
            val imuidsp = result.getFieldValue("imuids_p").toString
            val label = computeLabel(imuidsp)
            writer.println("%s\t%s\t%s".format(itemtitle, label, imuidsp))
          }
        } catch {
          case e: Throwable => { /* NOOP: just discard crap records */ }
        }
      })
    })
    Console.println("Processing complete")
    writer.flush()
    writer.close()
  }
  
  def removeBadSemanticGroups(input: File, output: File): Unit = {
    val writer = new PrintWriter(new FileWriter(output), true)
    var ln = 0
    Source.fromFile(input).
      getLines.
      foreach(line => {
        if (ln % 100 == 0) Console.println("Processing %d lines".format(ln))
        val Array(label, imuidsp) = line.split("\t")
        val cimuidsp = imuidsp.split(" ").
          map(pair => {
            val Array(imuid, score) = pair.split("\\$")
            (imuid, score.toDouble)
          }).
          filter(pair => isIncludedStyGroup(pair._1)).
          map(pair => pair._1 + "$" + pair._2).
          mkString(" ")
        if (StringUtils.isNotEmpty(cimuidsp))
          writer.println("%s\t%s".format(label, cimuidsp))
        ln = ln + 1
    })
    Console.println("Processing complete")
    writer.flush()
    writer.close()
  }
    
  def isIncludedStyGroup(imuid: String): Boolean = {
    val copt = Option[HealthConcept](qpe.getHealthConcept(imuid))
    copt match {
      case Some(concept) => {
        if (ExcludedStyGroups.contains(concept.getStyGroup())) false
        else true
      }
      case None => false
    }
  }

  def computeLabel(imuidsp: String): Integer = {
    val domains = imuidsp.split(" ").map(nvp => {
        val Array(imuid, score) = nvp.split("\\$")
        (imuid, score.toDouble)
      }).
      sortWith((a, b) => a._2 > b._2).
      map(pair => (pair._1, pair._2, getDomainId(pair._1))).
      filter(triple => triple._3 > -1).
      map(triple => triple._3)
    if (domains.size > 0) domains(0)
    else -1
  }
  
  def getDomainId(imuid: String): Int = {
    val copt = Option[HealthConcept](qpe.getHealthConcept(imuid))
    copt match {
      case Some(concept) => {
        val domains = GenSet() ++ concept.getDomains().map(_.toInt)
        val intersect = DomainKeys.intersect(domains)
        if (intersect.size > 0) intersect.toList(0)
        else -1
      }
      case None => -1
    }
  }  
  
  def train(infile: File, modelFile: File): Unit = {
    
    val matrices = scala.collection.mutable.Map[String,MapMatrix]()
  
    var ln = 0
    Source.fromFile(infile).getLines.
      foreach(line => {
        if (ln % 100 == 0) Console.println("Processing %d lines".format(ln))
        val Array(label, imuidsp) = line.split("\t")
        val matrix = matrices.getOrElse(label, new MapMatrix())
        matrix.addVector(MapVector.fromString(imuidsp, true))
        matrices(label) = matrix
        ln = ln + 1    
    })
    Console.println("Processing complete, building model")
  
    val model = new PrintWriter(new FileWriter(modelFile), true)
    val x = matrices.keySet.
      map(key => (key, matrices(key).centroid())).
      foreach(pair => model.println("%s\t%s".
      format(pair._1, MapVector.toFormattedString(pair._2))))
    Console.println("Model building complete")
    model.flush()
    model.close()
  }  
    
  def test(modelFile: File, testFile: File, reportFile: File): Unit = {
    
    val centroids = Source.fromFile(modelFile).getLines.
      map(line => {
        val cols = line.split("\t")
        (cols(0), MapVector.fromString(cols(1), true))
      }).
      toMap
    val writer = new PrintWriter(new FileWriter(reportFile), true)
    var numTests = 0.0D
    var numCorrect = 0.0D
    Source.fromFile(testFile).getLines.
      foreach(line => {
        if (numTests % 100 == 0) 
          Console.println("Processing %d lines".format(numTests.toInt))
        val Array(itemtitle, label, imuidsp) = line.split("\t")
        val catscores = centroids.keySet.map(key => {
            val vector = MapVector.fromString(imuidsp, true)
            (key, vector.cosim(centroids(key)))
          }).
          toList.
          sortWith((a, b) => a._2 > b._2)
        val scoresum = catscores.map(kv => kv._2).
          foldLeft(0.0D)(_ + _)
        val confidences = catscores.map(kv => 
          (kv._1, kv._2 * 100 / scoresum)).
          filter(kv => kv._2 > RecoPercentCutoff).
          slice(0, RecoNumber)
        writer.println(itemtitle) // title
        writer.println("\t" + confidences.map(kv => 
          new String("%s (%-5.2f%%)".format(DomainMappings(kv._1.toInt), kv._2))).
          mkString("; "))
        numTests = numTests + 1
        val recommended = confidences.map(_._1).toSet
        if (recommended.contains(label)) 
          numCorrect = numCorrect + 1
    })
    writer.flush()
    writer.close()
    Console.println("Accuracy(%) = " + (numCorrect / numTests) * 100)
  }
}
