package com.mycompany.scalcium.sherlock

import java.util.Properties

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.propertiesAsScalaMap

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP

class SentenceSplitter {
    
    val props = new Properties()
    props("annotators") = "tokenize, ssplit"
    val pipeline = new StanfordCoreNLP(props)
    
    def split(fileParaText: (Int, Int, String), doPadding: Boolean): 
            List[(Int, Int, Int, String)] = {
        val fileId = fileParaText._1
        val paraId = fileParaText._2
        val paraText = if (!doPadding) fileParaText._3
                       else "<START>. " + fileParaText._3
        val annot = new Annotation(paraText)
        pipeline.annotate(annot)
        annot.get(classOf[SentencesAnnotation])
             .map(sentence => sentence.toString())
             .zipWithIndex
             .map(sentWithId => 
                 (fileId, paraId, sentWithId._2, sentWithId._1))
             .toList
    }
}
