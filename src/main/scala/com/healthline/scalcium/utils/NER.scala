package com.healthline.scalcium.utils

import java.io.File
import java.util.regex.Pattern
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import com.aliasi.chunk.CharLmHmmChunker
import com.aliasi.chunk.Chunk
import com.aliasi.chunk.HmmChunker
import com.aliasi.chunk.RegExChunker
import com.aliasi.dict.DictionaryEntry
import com.aliasi.dict.ExactDictionaryChunker
import com.aliasi.dict.MapDictionary
import com.aliasi.hmm.HmmCharLmEstimator
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory
import com.aliasi.util.AbstractExternalizable
import com.aliasi.chunk.ChunkerEvaluator

trait NER {
  
  def chunk(s: String): List[Chunk]
  
  def tag(s: String): List[(String,String)] = {
    val chunks = chunk(s)
    var curr = 0
    val tags = new ArrayBuffer[(String,String)]
    chunks.map(chunk => {
      val start = chunk.start
      val end = chunk.end
      val ctype = chunk.`type`
      if (curr < start) {
        val prevtext = s.substring(curr, start)
        prevtext.split(" ")
          .foreach(word => tags += ((word, "O")))
      }
      val chunktext = s.substring(start, end)
      chunktext.split(" ")
        .foreach(word => tags += ((word, ctype)))
      curr = end
    })
    if (curr < s.length()) {
      val lasttext = s.substring(curr, s.length())
      lasttext.split(" ")
        .foreach(word => tags += ((word, "O")))
    }
    tags.filter(wt => wt._1.length() > 0)
      .toList
  }
  
  def merge(taggedWords: List[List[(String,String)]]): 
      List[(String,String)] = {
    val lengths = taggedWords.map(tag => tag.size)
    val maxlen = lengths.max
    val longest = lengths.zipWithIndex
      .sortBy(li => li._1)
      .head._2
    val words = taggedWords(longest).map(
      taggedWord => taggedWord._1)
    val mergedTags = (0 until maxlen).map(i => {
      val tags = taggedWords.map(taggedWord => 
        if (taggedWord.size > i) taggedWord(i)._2 else "O")
        .filter(tag => !"O".equals(tag))
      if (tags.isEmpty) "O"
      else tags.head // TODO: revisit use Bayes Net for disambig
    })
    .toList
    words.zip(mergedTags)
  }
}

/**
 * Dictionary based NER. Uses a set of files, each containing
 * terms that belong to a specified class.
 */
class DictNER(val data: Map[String,File]) extends NER {
  
  val dict = new MapDictionary[String]()
  data.foreach(entityData => {
    val entityName = entityData._1
    Source.fromFile(entityData._2).getLines()
      .foreach(line => dict.addEntry(
        new DictionaryEntry[String](line, entityName, 1.0D)))
  })
  val chunker = new ExactDictionaryChunker(dict, 
    IndoEuropeanTokenizerFactory.INSTANCE, false, false)

  override def chunk(s: String): List[Chunk] = {
    val chunking = chunker.chunk(s)
    chunking.chunkSet().iterator().toList
  }
}

/**
 * Regex based NER. Uses a set of files, each containing
 * regular expressions representing a specified class.
 */
class RegexNER(val data: Map[String,File]) extends NER {
  val chunkers = data.map(entityData => {
    val entityName = entityData._1
    Source.fromFile(entityData._2).getLines()
      .map(line => new RegExChunker(
        Pattern.compile(line), entityName, 1.0D))
  })
  .flatten
  .toList
  
  override def chunk(s: String): List[Chunk] = {
    chunkers.map(chunker => {
      val chunking = chunker.chunk(s)
      chunking.chunkSet().iterator()
    })
    .flatten
    .toList
    .sortBy(chunk => chunk.start)
  }
}

/**
 * Model based NER. A single multiclass HMM Language
 * Model is constructed out of the training data, and
 * used to predict classes for new words.
 */
class ModelNER(val modelFile: File) extends NER {
  val chunker = if (modelFile != null) 
    AbstractExternalizable
      .readObject(modelFile)
      .asInstanceOf[HmmChunker]
    else null
    
  override def chunk(s: String): List[Chunk] = {
    if (chunker == null) List.empty
    else chunker.chunk(s)
      .chunkSet()
      .iterator()
      .toList
  }

  def train(taggedFile: File, modelFile: File,
      ngramSize: Int, numChars: Int,
      lambda: Double): Unit = {
    val factory = IndoEuropeanTokenizerFactory.INSTANCE
    val estimator = new HmmCharLmEstimator(
      ngramSize, numChars, lambda)
    val chunker = new CharLmHmmChunker(factory, estimator)
    Source.fromFile(taggedFile)
      .getLines()
      .foreach(taggedWords => {
        taggedWords.split(" ")
        .foreach(taggedWord => {
          val slashAt = taggedWord.lastIndexOf('/')
          val word = taggedWord.substring(0, slashAt)
          val tag = taggedWord.substring(slashAt + 1)
          chunker.trainDictionary(word, tag)
      })
    })
    AbstractExternalizable.compileTo(chunker, modelFile)
  }
}