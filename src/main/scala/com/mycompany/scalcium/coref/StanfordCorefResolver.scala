package com.mycompany.scalcium.coref

import java.util.Properties
import scala.collection.JavaConversions._
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.util.IntPair
import edu.stanford.nlp.dcoref.CorefChain.CorefMention
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import scala.collection.mutable.ArrayBuffer

class StanfordCorefResolver extends CorefResolver {

  val props = new Properties()
  props("annotators") = "tokenize, ssplit, pos, lemma, ner, parse, dcoref"
  val pipeline = new StanfordCoreNLP(props)
  
  override def resolve(text: String): List[(CorefTriple,List[CorefTriple])] = {
    val doc = new Annotation(text)
    pipeline.annotate(doc)
    val sentences = doc.get(classOf[SentencesAnnotation])
      .map(coremap => coremap.get(classOf[TextAnnotation]))
      .toList
    val sentenceOffsets = buildSentenceOffsets(sentences)
    val graph = doc.get(classOf[CorefChainAnnotation])
    graph.values.map(chain => {
      val mention = chain.getRepresentativeMention()
      val ref = toTuple(mention, doc, sentenceOffsets)
      val comentions = chain.getMentionsInTextualOrder()
      val corefs = comentions.map(coref => toTuple(coref, doc, sentenceOffsets))
                             .filter(triple => ! ref.text.equals(triple.text))
                             .toList
      (ref, corefs)
    })
    .filter(tuple => tuple._2.size > 0)
    .toList
  }

  def max(a: Int, b: Int) = if (a > b) a else b
  def min(a: Int, b: Int) = if (a < b) a else b

  def toTuple(coref: CorefMention, doc: Annotation, soffsets: Map[Int,Int]): 
	  CorefTriple = {
    val sbegin = soffsets(coref.sentNum - 1)
    val mtriple = doc.get(classOf[SentencesAnnotation])
      // get sentence being analyzed
      .get(coref.sentNum - 1)
      // get all tokens in sentence with character offsets
      .get(classOf[TokensAnnotation])
      .map(token => ((token.originalText().toString(), 
          sbegin + token.beginPosition(), sbegin + token.endPosition())))
      // sublist the coreference part
      .subList(coref.startIndex - 1, coref.endIndex - 1)
      // join adjacent tokens into a single mention triple
      .foldLeft(("", Int.MaxValue, 0))((a, b) => 
        (List(a._1, b._1).mkString(" "), min(a._2, b._2), max(a._3, b._3)))
    CorefTriple(mtriple._1.trim(), mtriple._2, mtriple._3)
  }
  
  def buildSentenceOffsets(sentences: List[String]): Map[Int,Int] = {
    val slengths = sentences
      .zipWithIndex
      .map(si => (si._2, si._1.length()))
      .sortWith(_._1 > _._1)
    val soffsets = ArrayBuffer[(Int,Int)]()
    for (sindex <- slengths.map(_._1)) {
      val offset = if (sindex == 0) ((sindex, 0))
      else {
    	val rest = slengths.drop(slengths.size - sindex)
        val offset = rest.map(_._2).foldLeft(0)(_ + _)
        ((sindex, offset))
      }
      soffsets += offset
    }
    soffsets.toMap[Int,Int]
  }
}