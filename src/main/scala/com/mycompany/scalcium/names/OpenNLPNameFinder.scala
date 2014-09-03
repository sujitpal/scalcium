package com.mycompany.scalcium.names

import java.io.File
import java.io.FileInputStream
import org.apache.commons.io.IOUtils
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel
import scala.Array.canBuildFrom
import com.mycompany.scalcium.tokenizers.Tokenizer

class OpenNLPNameFinder extends NameFinder {

  val ModelDir = "src/main/resources/opennlp/models"
  
  val tokenizer = Tokenizer.getTokenizer("opennlp")
  val personME = buildME("en_ner_person.bin")
  val orgME = buildME("en_ner_organization.bin")
  
  override def find(sentences: List[String]): 
      List[List[(String,Int,Int)]] = {
    sentences.map(sentence => 
      find(personME, "PERSON", sentence) ++ 
        find(orgME, "ORGANIZATION", sentence))
  }
  
  def find(finder: NameFinderME, tag: String, 
      doc: List[String]): 
      List[List[(String,Int,Int)]] = {
    try {
      doc.map(sent => find(finder, tag, sent))
    } finally {
      clear(finder)
    }
  }
  
  def find(finder: NameFinderME, tag: String, sent: String): 
    List[(String,Int,Int)] = {
    val words = tokenizer.wordTokenize(sent)
                         .toArray
    finder.find(words).map(span => {
      val start = span.getStart()
      val end = span.getEnd()
      val coffsets = charOffset(start, end, words)
      (tag, coffsets._1, coffsets._2)
    }).toList
  }
  
  def clear(finder: NameFinderME): Unit = finder.clearAdaptiveData()
  
  def charOffset(wbegin: Int, wend: Int, words: Array[String]): 
      (Int,Int) = {
    val nstring = words.slice(wbegin, wend)
                       .mkString(" ")
    val sentence = words.mkString(" ")
    val cbegin = sentence.indexOf(nstring)
    val cend = cbegin + nstring.length()
    (cbegin, cend)
  }

  def buildME(model: String): NameFinderME = {
    var pfin: FileInputStream = null
    try {
      pfin = new FileInputStream(new File(ModelDir, model))
      new NameFinderME(new TokenNameFinderModel(pfin))
    } finally {
      IOUtils.closeQuietly(pfin)
    }
  }
}
