package com.mycompany.scalcium.names

import java.io.File
import scala.collection.JavaConversions._
import com.mycompany.scalcium.tokenizers.Tokenizer
import java.util.Properties
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.NormalizedNamedEntityTagAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation

class StanfordNameFinder extends NameFinder {

  val props = new Properties()
  props("annotators") = "tokenize, ssplit, pos, lemma, ner"
  props("ssplit.isOneSentence") = "true"
  val pipeline = new StanfordCoreNLP(props)

  override def find(sentences: List[String]): List[List[(String,Int,Int)]] = {
    sentences.map(sentence => {
      val sent = new Annotation(sentence)
      pipeline.annotate(sent)
      sent.get(classOf[SentencesAnnotation])
        .head
        .get(classOf[TokensAnnotation])
        .map(corelabel => (corelabel.ner(), corelabel.beginPosition(), 
          corelabel.endPosition()))
        .filter(triple => (! "O".equals(triple._1)))
        .groupBy(triple => triple._1)
        .map(kv => {
          val key = kv._1
          val list = kv._2
          val begin = list.sortBy(x => x._2).head._2
          val end = list.sortBy(x => x._3).reverse.head._3
          (key, begin, end)
        })
        .toList
    })
    .toList
  }
}
