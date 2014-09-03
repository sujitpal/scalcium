package com.mycompany.scalcium.names

import java.io.File
import java.io.FileInputStream
import org.apache.commons.io.IOUtils
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel
import scala.Array.canBuildFrom
import com.mycompany.scalcium.tokenizers.Tokenizer

class OpenNLPNameFinder {

  val ModelDir = "src/main/resources/opennlp/models"
  
  val tokenizer = Tokenizer.getTokenizer("opennlp")
  val personME = buildME("en_ner_person.bin")
  val orgME = buildME("en_ner_organization.bin")
  
  def find(finder: NameFinderME, doc: List[String]): 
      List[List[(String,Int,Int)]] = {
    try {
      doc.map(sent => find(finder, sent))
    } finally {
      clear(finder)
    }
  }
  
  def find(finder: NameFinderME, sent: String): 
    List[(String,Int,Int)] = {
    val words = tokenizer.wordTokenize(sent)
                         .toArray
    finder.find(words).map(span => {
      val start = span.getStart()
      val end = span.getEnd()
      val text = words.slice(start, end).mkString(" ")
      (text, start, end)
    }).toList
  }
  
  def clear(finder: NameFinderME): Unit = finder.clearAdaptiveData()
  
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
