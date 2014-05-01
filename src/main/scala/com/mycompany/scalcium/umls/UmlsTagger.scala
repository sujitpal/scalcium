package com.healthline.scalcium.umls

import java.io.File
import java.io.StringReader
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern

import scala.Array.canBuildFrom
import scala.Array.fallbackCanBuildFrom
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.Field.Index
import org.apache.lucene.document.Field.Store
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause.Occur
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.TermQuery
import org.apache.lucene.store.SimpleFSDirectory
import org.apache.lucene.util.Version

class UmlsTagger {

  val punctPattern = Pattern.compile("\\p{Punct}")
  val spacePattern = Pattern.compile("\\s+")
  
  def buildIndex(inputFile: File, 
      luceneDir: File): Unit = {
    // set up the index writer
    val analyzer = getAnalyzer()
    val iwconf = new IndexWriterConfig(Version.LUCENE_46, analyzer)
    iwconf.setOpenMode(IndexWriterConfig.OpenMode.CREATE)
    val writer = new IndexWriter(new SimpleFSDirectory(luceneDir), iwconf)
    // read through input file and write out to lucene
    val counter = new AtomicLong(0L)
    val linesReadCounter = new AtomicLong(0L)
    Source.fromFile(inputFile)
        .getLines()
        .foreach(line => {
      val linesRead = linesReadCounter.incrementAndGet()
      if (linesRead % 1000 == 0) Console.println("%d lines read".format(linesRead))
      val Array(cui, str) = line
        .replace("\",\"", "\t")
        .replaceAll("\"", "")
        .split("\t")
      val strNorm = normalizeCasePunct(str)
      val strSorted = sortWords(strNorm)
      val strStemmed = stemWords(strNorm)
      // write full str record 
      // str = exact string
      // str_norm = case and punct normalized, exact
      // str_sorted = str_norm sorted
      // str_stemmed = str_sorted stemmed
      val fdoc = new Document()
      val fid = counter.incrementAndGet()
      fdoc.add(new Field("id", fid.toString, Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("cui", cui, Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("str", str, Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("str_norm", strNorm, Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("str_sorted", strSorted, Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("str_stemmed", strStemmed, Store.YES, Index.NOT_ANALYZED))
      writer.addDocument(fdoc)
      if (fid % 1000 == 0) writer.commit()
    })
    writer.commit()
    writer.close()
  }

  def annotateConcepts(phrase: String, 
      luceneDir: File): 
      List[(Double,String,String)] = {
    val suggestions = ArrayBuffer[(Double,String,String)]()
    val reader = DirectoryReader.open(
      new SimpleFSDirectory(luceneDir)) 
    val searcher = new IndexSearcher(reader)
    // try to match full string
    suggestions ++= cascadeSearch(searcher, reader, 
      phrase, 1.0)
    if (suggestions.size == 0) {
      // no exact match found, fall back to inexact matches
      val words = normalizeCasePunct(phrase)
        .split(" ")
      val foundWords = scala.collection.mutable.Set[String]()
      for (nword <- words.size - 1 until 0 by -1) {
        words.sliding(nword)
          .map(ngram => ngram.mkString(" "))
          .foreach(ngram => {
            if (alreadySeen(foundWords, ngram)) {
              val ngramWords = ngram.split(" ")
              val ratio = ngramWords.size.toDouble / words.size
              val inexactSuggestions = cascadeSearch(
                searcher, reader, ngram, ratio)
              if (inexactSuggestions.size > 0) {
                suggestions ++= inexactSuggestions
                foundWords ++= ngramWords
              }
            }    
          })       
      }
    }
    if (suggestions.size > 0) {
      // dedup by cui, keeping the first matched
      val deduped = suggestions.groupBy(_._3)
        .map(kv => kv._2.head)
        .toList  
        .sortWith((a,b) => a._1 > b._1)
      suggestions.clear
      suggestions ++= deduped
    }
    // clean up
    reader.close()
    // return results
    suggestions.toList
  }
  
  def printConcepts(
      concepts: List[(Double,String,String)]): 
      Unit = {
    concepts.foreach(concept => 
      Console.println("[%6.2f%%] (%s) %s"
      .format(concept._1, concept._3, concept._2)))
  }
  
  def normalizeCasePunct(str: String): String = {
    val str_lps = punctPattern
      .matcher(str.toLowerCase())
      .replaceAll(" ")
    spacePattern.matcher(str_lps).replaceAll(" ")
  }

  def sortWords(str: String): String = {
    val words = str.split(" ")
    words.sortWith(_ < _).mkString(" ")
  }
  
  def stemWords(str: String): String = {
    val stemmedWords = ArrayBuffer[String]()
    val tokenStream = getAnalyzer().tokenStream(
      "str_stemmed", new StringReader(str))
    val ctattr = tokenStream.addAttribute(
      classOf[CharTermAttribute])    
    tokenStream.reset()
    while (tokenStream.incrementToken()) {
      stemmedWords += ctattr.toString()
    }
    stemmedWords.mkString(" ")
  }
  
  def getAnalyzer(): Analyzer = {
    new StandardAnalyzer(Version.LUCENE_46)
  }
  
  case class StrDocument(id: Int, 
      cui: String, str: String, 
      strNorm: String, strSorted: String, 
      strStemmed: String)
      
  def getDocument(reader: IndexReader,
      hit: ScoreDoc): StrDocument = {
    val doc = reader.document(hit.doc)
    StrDocument(doc.get("id").toInt,
      doc.get("cui"), doc.get("str"), 
      doc.get("str_norm"), doc.get("str_sorted"), 
      doc.get("str_stemmed"))
  }
  
  def cascadeSearch(searcher: IndexSearcher,
      reader: IndexReader, phrase: String,
      ratio: Double): 
      List[(Double,String,String)] = {
    val results = ArrayBuffer[(Double,String,String)]()
    // exact match (100.0%)
    val query1 = new TermQuery(new Term("str", phrase))
    val hits1 = searcher.search(query1, 1).scoreDocs
    if (hits1.size > 0) {
      results += hits1.map(hit => {
        val doc = getDocument(reader, hit)
        (100.0 * ratio, doc.str, doc.cui)
      })
      .toList
      .head
    }
    // match normalized phrase (75%)
    val normPhrase = normalizeCasePunct(phrase)
    if (results.size == 0) {
      val query2 = new TermQuery(new Term("str_norm", normPhrase))
      val hits2 = searcher.search(query2, 1).scoreDocs
      if (hits2.size > 0) {
        results += hits2.map(hit => {
          val doc = getDocument(reader, hit)
          (90.0 * ratio, doc.str, doc.cui)
        })
        .toList
        .head
      }
    }
    // match sorted phrase (50%)
    val sortedPhrase = sortWords(normPhrase)
    if (results.size == 0) {
      val query3 = new TermQuery(new Term("str_sorted", sortedPhrase))
      val hits3 = searcher.search(query3, 1).scoreDocs
      if (hits3.size > 0) {
        results += hits3.map(hit => {
          val doc = getDocument(reader, hit)
          (80.0 * ratio, doc.str, doc.cui)
        })
        .toList
        .head
      }
    }
    // match stemmed phrase (25%)
    val stemmedPhrase = stemWords(normPhrase)
    if (results.size == 0) {
      val query4 = new TermQuery(new Term("str_stemmed", stemmedPhrase))
      val hits4 = searcher.search(query4, 1).scoreDocs
      if (hits4.size > 0) {
        results += hits4.map(hit => {
          val doc = getDocument(reader, hit)
          (70.0 * ratio, doc.str, doc.cui)
        })
        .toList
        .head
      }
    }
    results.toList
  }
  
  def alreadySeen(
      refset: scala.collection.mutable.Set[String], 
      ngram: String): Boolean = {
    val words = ngram.split(" ")
    val nseen = words.filter(word => 
      !refset.contains(word))
      .size
    if (refset.size == 0) true
    else if (nseen > 0) true 
    else false
  }
}