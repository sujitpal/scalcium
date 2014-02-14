package com.healthline.scalcium.umls

import java.io.File
import scala.io.Source
import java.util.regex.Pattern
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.util.Version
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.store.SimpleFSDirectory
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.Field.Store
import org.apache.lucene.document.Field.Index
import java.util.concurrent.atomic.AtomicLong
import org.apache.lucene.analysis.standard.StandardAnalyzer

class UmlsTagger {

  val punctPattern = Pattern.compile("\\p{Punct}")
  val spacePattern = Pattern.compile("\\s+")
  
  def build(inputFile: File, 
      luceneDir: File): Unit = {
    // set up the index writer
    val analyzer = new StandardAnalyzer(Version.LUCENE_46)
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
      if (linesRead % 10 == 0) Console.println("%d lines read".format(linesRead))
      val Array(cui, str) = line.split("\t")
      val strNorm = normalizeCasePunct(str)
      val words = strNorm.split(" ")
      val strSorted = words.sortWith(_ < _)
        .foldLeft("")(_ + " " + _)
      // write full str record
      val fdoc = new Document()
      val fid = counter.incrementAndGet()
      fdoc.add(new Field("id", String.valueOf(fid), Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("porf", "F", Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("cui", cui, Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("str", str, Store.YES, Index.NOT_ANALYZED))
      fdoc.add(new Field("str_norm", strNorm, Store.YES, Index.NOT_ANALYZED))
      writer.addDocument(fdoc)
      // write partial str (word) records
      words.foreach(word => {
    	val numWords = words.size
        val pdoc = new Document()
    	val pid = counter.incrementAndGet()
        pdoc.add(new Field("id", String.valueOf(pid), Store.YES, Index.NOT_ANALYZED))
        pdoc.add(new Field("porf", "P", Store.YES, Index.NOT_ANALYZED))
        pdoc.add(new Field("cui", cui, Store.YES, Index.NOT_ANALYZED))
        pdoc.add(new Field("word_s", word, Store.YES, Index.NOT_ANALYZED))
        pdoc.add(new Field("word_t", word, Store.YES, Index.ANALYZED))
        pdoc.add(new Field("nwords", String.valueOf(numWords), Store.YES, Index.NO))
        writer.addDocument(pdoc)
        if (pid % 10 == 0) writer.commit()
      })
      if (fid % 10 == 0) writer.commit()
    })
    writer.close()
  }
  
  def normalizeCasePunct(str: String): String = {
    val str_lps = punctPattern
      .matcher(str.toLowerCase())
      .replaceAll(" ")
    spacePattern.matcher(str_lps).replaceAll(" ")
  }
}