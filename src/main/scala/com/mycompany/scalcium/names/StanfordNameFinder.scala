package com.mycompany.scalcium.names

import java.io.File

import scala.collection.JavaConversions._

import com.mycompany.scalcium.tokenizers.Tokenizer

import edu.stanford.nlp.ie.AbstractSequenceClassifier
import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreLabel

class StanfordNameFinder extends NameFinder {

  val ModelDir = "src/main/resources/stanford"

  val tokenizer = Tokenizer.getTokenizer("opennlp")
  val classifier = buildClassifier(
    "english.conll.4class.distsim.crf.ser.gz")
  
  override def find(sentences: List[String]): 
      List[List[(String,Int,Int)]] = {
    sentences.map(sentence => 
      classifier.classifyToCharacterOffsets(sentence)
        .map(triple => (triple.first, 
          triple.second.toInt, triple.third.toInt))
        .toList)
  }
  
  def buildClassifier(model: String): 
      AbstractSequenceClassifier[CoreLabel] = {
    val modelfile = new File(ModelDir, model)
    CRFClassifier.getClassifier(modelfile)
  }
}