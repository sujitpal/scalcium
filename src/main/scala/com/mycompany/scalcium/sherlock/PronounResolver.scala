package com.mycompany.scalcium.sherlock

import java.util.Properties

import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.JavaConversions.propertiesAsScalaMap

import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP

class PronounResolver {
    
    val props = new Properties()
    props("annotators") = "tokenize, ssplit, pos, lemma, ner, parse, dcoref"
    val pipeline = new StanfordCoreNLP(props)

    def resolve(sentencePair: Array[(Int, Int, Int, String)]):
            (Int, Int, Int, String) = {
        val first = sentencePair(0)._4
        val second = sentencePair(1)._4
        if (first.equals("<START>.")) sentencePair(1)
        else if (second.equals("<START>.")) (-1, 0, 0, "")
        else {
            val text = Array(first, second).mkString(" ")
            val annot = new Annotation(text)
            pipeline.annotate(annot)
            val x = annot.get(classOf[CorefChainAnnotation]).values
                 .map(chain => {
                     val mention = chain.getRepresentativeMention()
                     mention
                 })
            Console.println(x)
            
            sentencePair(1)
        }
    }
}
