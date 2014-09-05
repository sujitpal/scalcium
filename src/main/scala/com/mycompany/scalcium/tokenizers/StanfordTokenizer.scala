package com.mycompany.scalcium.tokenizers

import java.util.Properties

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.trees.Tree
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation

class StanfordTokenizer extends Tokenizer {

  val props = new Properties()
  props("annotators") = "tokenize, ssplit, pos, parse"
  val pipeline = new StanfordCoreNLP(props)

  override def sentTokenize(para: String): List[String] = {
    val doc = new Annotation(para)
    pipeline.annotate(doc)
    doc.get(classOf[SentencesAnnotation])
      .map(coremap => coremap.get(classOf[TextAnnotation]))
      .toList
  }
  
  override def wordTokenize(sentence: String): List[String] = {
    val sent = new Annotation(sentence)
    pipeline.annotate(sent)
    sent.get(classOf[SentencesAnnotation])
      .head
      .get(classOf[TokensAnnotation])
      .map(corelabel => corelabel.get(classOf[TextAnnotation]))
      .toList
  }
  
  override def posTag(sentence: String): List[(String,String)]= {
    val sent = new Annotation(sentence)
    pipeline.annotate(sent)
    sent.get(classOf[SentencesAnnotation])
      .head
      .get(classOf[TokensAnnotation])
      .map(corelabel => {
        val word = corelabel.get(classOf[TextAnnotation])
        val tag = corelabel.get(classOf[PartOfSpeechAnnotation])
        (word, tag)
      })
      .toList
  }
  
  override def phraseChunk(sentence: String): List[(String,String)] = {
    val sent = new Annotation(sentence)
    pipeline.annotate(sent)
    val tree = sent.get(classOf[SentencesAnnotation])
      .head
      .get(classOf[TreeAnnotation])
    val chunks = ArrayBuffer[(String,String)]()
    extractChunks(tree, chunks)
    chunks.toList
  }
  
  def extractChunks(tree: Tree, chunks: ArrayBuffer[(String,String)]): Unit = {
    tree.children().map(child => {
      val tag = child.value()
      if (child.isPhrasal()) {
        // concatenate leaves under this to form phrase
        val phrase = child.getLeaves[Tree]()
          .flatMap(leaf => leaf.yieldWords())
          .map(word => word.word())
          .mkString(" ")
        chunks += ((phrase, tag))
      }
      // dig deeper
      extractChunks(child, chunks)
    })
  }
}