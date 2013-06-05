package com.github.scalcium

import java.io.{File, FileInputStream}

import org.apache.commons.io.IOUtils

import opennlp.tools.chunker.{ChunkerME, ChunkerModel}
import opennlp.tools.postag.{POSModel, POSTaggerME}
import opennlp.tools.sentdetect.{SentenceDetectorME, SentenceModel}
import opennlp.tools.tokenize.{TokenizerME, TokenizerModel}

class Tokenizer {

  val ModelDir = "/prod/common/data/opennlp"

  val sentenceDetectorFn = (model: SentenceModel) =>
    new SentenceDetectorME(model)    
  val sentenceDetector = sentenceDetectorFn({
    var smis: FileInputStream = null
    try {
      smis = new FileInputStream(new File(ModelDir, "en-sent.bin"))
      val model = new SentenceModel(smis)
      model
    } finally {
      IOUtils.closeQuietly(smis)
    }   
  })
  val tokenizerFn = (model: TokenizerModel) => 
    new TokenizerME(model)
  val tokenizer = tokenizerFn({
    var tmis: FileInputStream = null
    try {
      tmis = new FileInputStream(new File(ModelDir, "en-token.bin"))
      val model = new TokenizerModel(tmis)
      model
    } finally {
      IOUtils.closeQuietly(tmis)
    }
  })
  val posTaggerFn = (model: POSModel) => 
    new POSTaggerME(model)
  val posTagger = posTaggerFn({
    var pmis: FileInputStream = null
    try {
      pmis = new FileInputStream(new File(ModelDir, "en-pos-maxent.bin"))
      val model = new POSModel(pmis)
      model
    } finally {
      IOUtils.closeQuietly(pmis)
    }
  })
  val chunkerFn = (model: ChunkerModel) => 
    new ChunkerME(model)
  val chunker = chunkerFn({
    var cmis: FileInputStream = null
    try {
      cmis = new FileInputStream(new File(ModelDir, "en-chunker.bin"))
      val model = new ChunkerModel(cmis)
      model
    } finally {
      IOUtils.closeQuietly(cmis)
    }
  })

  def sentTokenize(text: String): List[String] = {
    sentenceDetector.sentDetect(text).toList
  }
  
  def phraseTokenize(sentence: String): List[String] = {
    val tokenSpans = tokenizer.tokenizePos(sentence)
    val tokens = tokenSpans.map(span => 
      span.getCoveredText(sentence).toString())
    val tags = posTagger.tag(tokens)
    return chunker.chunkAsSpans(tokens, tags).map(chunk => {
      val start = tokenSpans(chunk.getStart()).getStart()
      val end = tokenSpans(chunk.getEnd() - 1).getEnd()
      sentence.substring(start, end)
    }).toList
  }
  
  def wordTokenize(sentence: String): List[String] = {
    return tokenizer.tokenize(sentence).toList
  }
}