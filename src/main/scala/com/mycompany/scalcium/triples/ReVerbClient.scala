package com.mycompany.scalcium.triples

import java.io.StringReader

import scala.collection.JavaConversions._

import edu.washington.cs.knowitall.extractor.ReVerbExtractor
import edu.washington.cs.knowitall.normalization.BinaryExtractionNormalizer
import edu.washington.cs.knowitall.util.DefaultObjects

class ReVerbClient {

  def parse(text: String): List[(String,String,String)] = {
    DefaultObjects.initializeNlpTools()
    val reader = DefaultObjects.getDefaultSentenceReader(
      new StringReader(text))
    val extractor = new ReVerbExtractor()
    val normalizer = new BinaryExtractionNormalizer()
    reader.iterator()
      .flatMap(sent => extractor.extract(sent))
      .map(extract => {
        val normExtract = normalizer.normalize(extract)
        val subj = normExtract.getArgument1().toString()
        val verb = normExtract.getRelation().toString()
        val obj = normExtract.getArgument2().toString()
        (subj, verb, obj)
      })
      .toList
  }
}