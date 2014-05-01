package com.mycompany.scalcium.transformers

import java.io.File
import java.util.regex.Pattern

import scala.Array.canBuildFrom
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import com.aliasi.chunk.Chunker
import com.aliasi.dict.DictionaryEntry
import com.aliasi.dict.ExactDictionaryChunker
import com.aliasi.dict.MapDictionary
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory
import com.mycompany.scalcium.utils.Tokenizer

class TemporalAnnotator(val tempoWNFile: File) {

  val targets = List("Past", "Present", "Future")
  val chunkers = buildChunkers(tempoWNFile)
  val tokenizer = Tokenizer.getTokenizer("opennlp")
  
  val ptPoss = List("JJ", "NN", "VB", "RB")
    .map(p => Pattern.compile(p + ".*"))
  val wnPoss = List("s", "n", "v", "r")
  
  def predict(sentence: String): String = {
    val scoreTargetPairs = chunkers.map(chunker => {
      val taggedSentence = tokenizer.posTag(sentence)
        .map(wtp => wtp._1.replaceAll("\\p{Punct}", "") + 
          "/" + wordnetPos(wtp._2))
        .mkString(" ")
      val chunking = chunker.chunk(taggedSentence)
      chunking.chunkSet().iterator().toList
        .map(chunk => chunk.score())
        .foldLeft(0.0D)(_ + _)
      })
      .zipWithIndex
      .filter(stp => stp._1 > 0.0D)
    if (scoreTargetPairs.isEmpty) "Present"
    else {
      val bestTarget = scoreTargetPairs
        .sortWith((a,b) => a._1 > b._1)
        .head._2
      targets(bestTarget)
    }
  } 
  
  def buildChunkers(datafile: File): List[Chunker] = {
    val dicts = ArrayBuffer[MapDictionary[String]]()
    Range(0, targets.size).foreach(i => 
      dicts += new MapDictionary[String]())
    val pwps = scala.collection.mutable.Set[String]()
    Source.fromFile(datafile).getLines()
      .filter(line => (!(line.isEmpty() || line.startsWith("#"))))
      .foreach(line => {
        val cols = line.split("\\s{2,}")
        val wordPos = getWordPos(cols(1))
        val probs = cols.slice(cols.size - 4, cols.size)
          .map(x => x.toDouble)
        if (! pwps.contains(wordPos)) {
          Range(0, targets.size).foreach(i => 
            dicts(i).addEntry(new DictionaryEntry[String](
              wordPos, targets(i), probs(i))))
        }
        pwps += wordPos
    })
    val chunkers = new ArrayBuffer[Chunker]()
    dicts.map(dict => new ExactDictionaryChunker(
        dict, IndoEuropeanTokenizerFactory.INSTANCE, 
        false, false))
      .toList
  }
  
  def getWordPos(synset: String): String = {
    val sscols = synset.split("\\.")
    val words = sscols.slice(0, sscols.size - 2)
    val pos = sscols.slice(sscols.size - 2, sscols.size - 1).head
    words.mkString("")
      .split("_")
      .map(word => word + "/" + (if ("s".equals(pos)) "a" else pos))
      .mkString(" ")
  }  
  
  def wordnetPos(ptPos: String): String = {
    val matchIdx = ptPoss.map(p => p.matcher(ptPos).matches())
      .zipWithIndex
      .filter(mip => mip._1)
      .map(mip => mip._2)
    if (matchIdx.isEmpty) "o" else wnPoss(matchIdx.head)
  }
}
