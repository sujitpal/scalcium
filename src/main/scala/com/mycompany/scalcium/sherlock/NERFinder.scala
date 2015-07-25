package com.mycompany.scalcium.sherlock

import java.util.Properties

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.propertiesAsScalaMap
import scala.collection.mutable.Stack

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP

class NERFinder {

    val props = new Properties()
    props("annotators") = "tokenize, ssplit, pos, lemma, ner"
    props("ssplit.isOneSentence") = "true"
    val pipeline = new StanfordCoreNLP(props)

    def find(sent: (Int, Int, Int, String)): 
            List[(Int, Int, Int, String, String)] = {
        val fileId = sent._1
        val paraId = sent._2
        val sentId = sent._3
        val sentText = sent._4
        val annot = new Annotation(sentText)
        pipeline.annotate(annot)
        val tokTags = annot.get(classOf[SentencesAnnotation])
                           .head // only one sentence in input
                           .get(classOf[TokensAnnotation])
                           .map(token => {
                                val begin = token.beginPosition()
                                val end = token.endPosition()
                                val nerToken = sentText.substring(begin, end)
                                val nerTag = token.ner()
                                (nerToken, nerTag)
                           })
        // consolidate NER for multiple tokens into one, so
        // for example: Ronald/PERSON Adair/PERSON becomes 
        // "Ronald Adair"/PERSON
        val spans = Stack[(String, String)]()
        tokTags.foreach(tokTag => {
            if (spans.isEmpty) spans.push(tokTag)
            else if (tokTag._2.equals("O")) spans.push(tokTag)
            else {
                val prevEntry = spans.pop
                if (prevEntry._2.equals(tokTag._2))
                    spans.push((Array(prevEntry._1, tokTag._1).mkString(" "), 
                        tokTag._2))
                else {
                    spans.push(prevEntry)
                    spans.push(tokTag)
                }
            }
        })
        spans.reverse
             .filter(tokTag => !tokTag._2.equals("O"))
             .map(tokTag => (fileId, paraId, sentId, tokTag._1, tokTag._2))
             .toList
    }
}
