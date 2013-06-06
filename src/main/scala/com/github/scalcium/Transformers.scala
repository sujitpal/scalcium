package com.github.scalcium

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

import com.healthline.query.QueryEngine
import com.healthline.query.kb.HealthConcept

case class ConceptScore (
  baseScore: Float,
  relScore: Float,
  ancestorScore: Float,
  finalScore: Float
)

case class Doc (
  contentType: String,
  title: String, 
  keywords: String, 
  body: String,
  // placeholder for pipeline to update
  paragraphs: List[String],
  sentences: List[String],
  phrases: List[String],
  words: List[String],
  title_cp: String,
  body_cp: String,
  concepts: List[HealthConcept]
)

class Transformers {
  
  val chain = Seq[Function1[Doc,Doc]](
    processParagraphs,
    processSentences,
    processPhrases
//    buildTokenTree,
//    setBoostFactors,
//    countWords,
//    setTermOccurrences,
//    setConceptOccurrences,
//    filterBodyBaseOnlyScore,
//    adjustExpertConsultStips,
//    calculateBaseScores,
//    filterConceptsWithStyCodes,
//    filterConceptsWithStyGroups,
//    adjustLargeBaseScores,
//    calculateRelationshipScores,
//    adjustLargeRelationshipScores,
//    calculateTitleScore,
//    calculateAgeGroup,
//    calculateFinalScore,
//    adjustFinalScore,
//    injectBNRConcepts,
//    aggregateDemographics,
//    setArticleGroup,
//    setAdCategory,
//    calculateSummary,
//    setConceptPositions
  )
  
  val tokenizer = new Tokenizer()
  val qpe = QueryEngine.getQueryService()
  
  def transform(doc: Doc): Doc = transform_r(doc, chain)
  
  @tailrec
  private final def transform_r(doc: Doc, 
      chain: Seq[Function1[Doc,Doc]]): Doc = {
    if (chain.isEmpty) doc
    else transform_r(chain.head(doc), chain.tail)
  }
  
  def shouldPerform(doc: Doc, key: String): Boolean = {
    ConfigUtils.getBooleanValue(doc.contentType, key, false)  
  }
  
  def processParagraphs = (doc: Doc) => {
    if (! shouldPerform(doc, "processParagraphs")) doc
    else {
      // TODO: any para level work?
      val odoc = Doc(doc.contentType, doc.title, doc.keywords, 
    	doc.body, tokenizer.paraTokenize(doc.body), null, null, 
    	null, null, null, null)
      odoc
    }
  }
  
  def processSentences = (doc: Doc) => {
    if (! shouldPerform(doc, "processSentences")) doc
    else {
      val sbuf = new ArrayBuffer[String]()
      doc.paragraphs.foreach(paragraph => {
        val sentences = tokenizer.sentTokenize(paragraph)
        // TODO: do sentence level work here
        sbuf ++ sentences
      })
      val odoc = Doc(doc.contentType, doc.title, doc.keywords, 
        doc.body, doc.paragraphs, sbuf.toList, null, null, 
        null, null, null)
      odoc
    }
  }
  
  // TODO: insert any functions that operate on sentences here
  
  def processPhrases = (doc: Doc) => {
    if (! shouldPerform(doc, "processPhrases")) doc
    else {
      val phbuf = new ArrayBuffer[String]()
      doc.sentences.foreach(sentence => { 
        val phrases = tokenizer.phraseTokenize(sentence)
        // TODO: do phrase level work here
        phbuf ++ phrases
  	  })
  	  val odoc = Doc(doc.contentType, doc.title, doc.keywords, 
  	    doc.body, doc.paragraphs, doc.sentences, phbuf.toList, 
  	    null, null, null, null)
  	  odoc
    }
  }

  // TODO: insert functions that operate on phrases here
  
}