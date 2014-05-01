package com.mycompany.scalcium.utils

import java.io.File

import scala.collection.JavaConversions.{asScalaBuffer, asScalaSet, bufferAsJavaList, seqAsJavaList}
import scala.collection.immutable.List
import scala.collection.mutable.ArrayBuffer

import com.aliasi.chunk.{ChunkFactory, Chunker, Chunking, ChunkingImpl}
import com.aliasi.hmm.{HiddenMarkovModel, HmmDecoder}
import com.aliasi.sentences.{IndoEuropeanSentenceModel, SentenceChunker}
import com.aliasi.tokenizer.{IndoEuropeanTokenizerFactory, TokenizerFactory}
import com.aliasi.util.{AbstractExternalizable, FastCache, Strings}

class LingPipeTokenizer extends Tokenizer {

  val PosModel = "/lingpipe/models/pos-en-general-brown.HiddenMarkovModel"
    
  val cache = new FastCache[String,Array[Double]](50000)
  val tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE
  val posHmm = AbstractExternalizable.readResourceObject(PosModel). 
    asInstanceOf[HiddenMarkovModel]

  def sentTokenize(para: String): List[String] = {
    val model = new IndoEuropeanSentenceModel()
    val chunker = new SentenceChunker(tokenizerFactory, model)
    val chunking = chunker.chunk(para.toCharArray, 0, para.length)
    val chunkset = chunking.chunkSet()
    val sentences = new ArrayBuffer[String]()
    if (chunkset.size() > 0) {
      val slice = chunking.charSequence().toString()
      val it = chunkset.iterator()
      while (it.hasNext()) {
        val sentence = it.next()
        val start = sentence.start()
        val end = sentence.end()
        sentences += slice.substring(start, end)
      }
    }
    sentences.toList
  }

  def wordTokenize(sentence: String): List[String] = {
    val tokenizer = tokenizerFactory.tokenizer(sentence.toCharArray(),
      0, sentence.length())
    val it = tokenizer.iterator()
    val words = new ArrayBuffer[String]()
    while (it.hasNext()) {
      words += it.next()
    }
    words.toList
  }

  def posTag(sentence: String): List[(String, String)] = {
    val decoder = new HmmDecoder(posHmm, null, cache)
    val tokens = wordTokenize(sentence)
    val tagging = decoder.tag(tokens)
    tagging.tokens().zip(tagging.tags().
      map(_.toUpperCase())).toList
  }

  def phraseChunk(sentence: String): List[(String,String)] = {
    val posTagger = new HmmDecoder(posHmm, null, cache)
    val chunker = new PhraseChunker(posTagger, tokenizerFactory)
    val tokens = tokenizerFactory.tokenizer(
      sentence.toCharArray, 0, sentence.length).tokenize()
    val tagging = posTagger.tag(tokens.toList)
    val chunking = chunker.chunk(sentence)
    val chunkText = chunking.charSequence()
    chunking.chunkSet().map(chunk => {
      val start = chunk.start()
      val end = chunk.end()
      val chunkType = chunk.`type`
      (chunkText.subSequence(start, end).toString(), chunkType)
    }).toList
  }
}

class PhraseChunker(posTagger: HmmDecoder, 
    tokenizerFactory: TokenizerFactory) extends Chunker {
  
  val PunctuationTags = Set[String]("'", ".", "*")
  val StartNounTags = Set[String](
    // determiner tags
    "abn", "abx", "ap", "ap$", "at", "cd", "cd$", "dt", "dt$",
    "dti", "dts", "dtx", "od",
    // adjective tags
    "jj", "jj$", "jjr", "jjs", "jjt", "*", "ql",
    // noun tags
    "nn", "nn$", "nns", "nns$", "np", "np$", "nps", "nps$",
    "nr", "nr$", "nrs",
    // pronoun tags
    "pn", "pn$", "pp$", "pp$$", "ppl", "ppls", "ppo", "pps", "ppss")
  val ContinueNounTags = Set[String](
    // adverb tags  
    "rb", "rb$", "rbr", "rbt", "rn", "ql", "*") ++ 
    StartNounTags ++ PunctuationTags
  val StartVerbTags = Set[String](
    // verb tags
    "vb", "vbd", "vbg", "vbn", "vbz",
    // auxilliary verb tags
    "to", "md", "be", "bed", "bedz", "beg", "bem", "ben", "ber", "bez",
    // adverb tags
    "rb", "rb$", "rbr", "rbt", "rn", "ql", "*"
  )
  val ContinueVerbTags = PunctuationTags ++ StartVerbTags
  
  override def chunk(cseq: CharSequence): Chunking = {
    val cs = Strings.toCharArray(cseq)
    return chunk(cs, 0, cs.length)
  }
  
  override def chunk(cs: Array[Char], start: Int, end: Int): Chunking = {
    val tokenList = ArrayBuffer[String]()
    val whiteList = ArrayBuffer[String]()
    val tokenizer = tokenizerFactory.tokenizer(cs, start, end - start)
    tokenizer.tokenize(tokenList, whiteList)
    val tagging = posTagger.tag(tokenList)
    val chunking = new ChunkingImpl(cs, start, end)
    var startChunk = 0
    var endChunk = 0
    var trimmedEndChunk = 0
    var i = 0
    while (i < tagging.size()) {
      startChunk += whiteList(i).size
      if (StartNounTags.contains(tagging.tag(i))) {
        endChunk = startChunk + tokenList(i).size
        i += 1
        while (i < tokenList.size() && 
            ContinueNounTags.contains(tagging.tag(i))) {
          endChunk += whiteList(i).size + tokenList(i).size
          i += 1
        }
        trimmedEndChunk = endChunk
        var k = i - 1
        while (k >= 0 && PunctuationTags.contains(tagging.tag(k))) {
          trimmedEndChunk -= whiteList(k).size + tokenList(k).size
          k -= 1
        }
        if (startChunk >= trimmedEndChunk) {
          startChunk = endChunk
        } else {
          val chunk = ChunkFactory.createChunk(startChunk, trimmedEndChunk, "noun")
          chunking.add(chunk)
          startChunk = endChunk
        }
      } else if (StartVerbTags.contains(tagging.tag(i))) {
        endChunk = startChunk + tokenList(i).size
        i += 1
        while (i < tokenList.size && 
            ContinueVerbTags.contains(tagging.tag(i))) {
          endChunk += whiteList(i).size + tokenList(i).size
          i += 1
        }
        trimmedEndChunk = endChunk
        var k = i - 1
        while (k >= 0 && PunctuationTags.contains(tagging.tag(k))) {
          trimmedEndChunk -= whiteList(k).size + tokenList(k).size
          k -= 1
        }
        if (startChunk >= trimmedEndChunk) {
          startChunk = endChunk
        } else {
          val chunk = ChunkFactory.createChunk(startChunk, trimmedEndChunk, "verb")
          chunking.add(chunk)
          startChunk = endChunk
        }
      } else {
        startChunk += tokenList(i).size
        i += 1
      }
    }
    return chunking
  }
}
