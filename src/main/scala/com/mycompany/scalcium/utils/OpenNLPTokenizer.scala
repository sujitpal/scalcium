package com.mycompany.scalcium.utils

import java.io.InputStream

import scala.Array.canBuildFrom

import org.apache.commons.io.IOUtils

import opennlp.tools.chunker.{ChunkerME, ChunkerModel}
import opennlp.tools.postag.{POSModel, POSTaggerME}
import opennlp.tools.sentdetect.{SentenceDetectorME, SentenceModel}
import opennlp.tools.tokenize.{TokenizerME, TokenizerModel}

class OpenNLPTokenizer extends Tokenizer {

  val ModelDir = "/opennlp/models"

  val sentenceDetectorFn = (model: SentenceModel) =>
    new SentenceDetectorME(model)    
  val sentenceDetector = sentenceDetectorFn({
    var smis: InputStream = null
    try {
      smis = getClass.getResourceAsStream(
        List(ModelDir, "en_sent.bin").mkString("/"))
      val model = new SentenceModel(smis)
      model
    } finally {
      IOUtils.closeQuietly(smis)
    }   
  })
  val tokenizerFn = (model: TokenizerModel) => 
    new TokenizerME(model)
  val tokenizer = tokenizerFn({
    var tmis: InputStream = null
    try {
      tmis = getClass.getResourceAsStream(
        List(ModelDir, "en_token.bin").mkString("/"))
      val model = new TokenizerModel(tmis)
      model
    } finally {
      IOUtils.closeQuietly(tmis)
    }
  })
  val posTaggerFn = (model: POSModel) => 
    new POSTaggerME(model)
  val posTagger = posTaggerFn({
    var pmis: InputStream = null
    try {
      pmis = getClass.getResourceAsStream(
        List(ModelDir, "en_pos_maxent.bin").mkString("/"))
      val model = new POSModel(pmis)
      model
    } finally {
      IOUtils.closeQuietly(pmis)
    }
  })
  val chunkerFn = (model: ChunkerModel) => 
    new ChunkerME(model)
  val chunker = chunkerFn({
    var cmis: InputStream = null
    try {
      cmis = getClass.getResourceAsStream(
        List(ModelDir, "en_chunker.bin").mkString("/"))
      val model = new ChunkerModel(cmis)
      model
    } finally {
      IOUtils.closeQuietly(cmis)
    }
  })

  def sentTokenize(para: String): List[String] = {
    sentenceDetector.sentDetect(para).toList
  }
  
  def wordTokenize(sentence: String): List[String] = {
    return tokenizer.tokenize(sentence).toList
  }
  
  def posTag(sentence: String): List[(String,String)] = {
    val tokenSpans = tokenizer.tokenizePos(sentence)
    val tokens = tokenSpans.map(span => 
      span.getCoveredText(sentence).toString())
    val tags = posTagger.tag(tokens)
    tokens.zip(tags).toList
  }
  
  def phraseChunk(sentence: String): List[(String,String)] = {
    val tokenSpans = tokenizer.tokenizePos(sentence)
    val tokens = tokenSpans.map(span => 
      span.getCoveredText(sentence).toString())
    val tags = posTagger.tag(tokens)
    return chunker.chunkAsSpans(tokens, tags).map(chunk => {
      val start = tokenSpans(chunk.getStart()).getStart()
      val end = tokenSpans(chunk.getEnd() - 1).getEnd()
      (sentence.substring(start, end), chunk.getType())
    }).toList
  }
}
