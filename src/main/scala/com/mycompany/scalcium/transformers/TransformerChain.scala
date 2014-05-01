package com.healthline.scalcium.transformers

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

import com.healthline.query.QueryEngine
import com.healthline.query.kb.HealthConcept
import com.healthline.scalcium.utils.ConfigUtils
import com.healthline.scalcium.utils.Tokenizer

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
  scores: Map[HealthConcept,Float],
  context: Map[String,Any]
)

class TransformerChain {
  
  val chain = Seq[Function1[Doc,Doc]](
    setContext,
    // operate on raw text
    expandAbbreviations,
    // operate on paragraphs
    processParagraphs,
    // operate on sentences
    processSentences,
    handleNegation,
    // operate on phrases
    processPhrases,
    calculateBaseScores
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
  
  def transform(doc: Doc): Doc = transform_r(doc, chain)
  
  @tailrec
  private final def transform_r(doc: Doc, 
      chain: Seq[Function1[Doc,Doc]]): Doc = {
    if (chain.isEmpty) doc
    else transform_r(chain.head(doc), chain.tail)
  }
  
  def shouldPerform(doc: Doc, key: String): Boolean = {
    ConfigUtils.getBooleanValue(doc.contentType, key, true)  
  }
  
  ////////////////////////////////////////////////////////////////
  
  def setContext = (doc: Doc) => {
    doc.copy(context=ContextSetter.setContext(doc))
  }
  
  def expandAbbreviations = (doc: Doc) => {
    if (! shouldPerform(doc, "expandAbbreviations")) doc
    else doc.copy(body=AbbreviationExpander.expand(doc.body, 
      doc.context("tokenizer").asInstanceOf[Tokenizer]))
  }
  
  def processParagraphs = (doc: Doc) => {
    if (! shouldPerform(doc, "processParagraphs")) doc
    else doc.copy(paragraphs=doc.context("tokenizer").
      asInstanceOf[Tokenizer].paraTokenize(doc.body))
  }
  
  def processSentences = (doc: Doc) => {
    if (! shouldPerform(doc, "processSentences")) doc
    else {
      val sbuf = new ArrayBuffer[String]()
      val tokenizer = doc.context("tokenizer").
        asInstanceOf[Tokenizer]
      doc.paragraphs.foreach(paragraph => {
        val sentences = tokenizer.sentTokenize(paragraph)
        sbuf ++= sentences
      })
      doc.copy(sentences=sbuf.toList)
    }
  }
  
  def handleNegation = (doc: Doc) => {
    if (! shouldPerform(doc, "handleNegation")) doc
    else {
      val tokenizer = doc.context("tokenizer").asInstanceOf[Tokenizer]
      val osentences = doc.sentences.map(sentence =>
        NegationHandler.maskNegative(sentence, tokenizer))
      doc.copy(sentences=osentences)
    }
  }
  
  def processPhrases = (doc: Doc) => {
    if (! shouldPerform(doc, "processPhrases")) doc
    else {
      val phbuf = new ArrayBuffer[String]()
      val tokenizer = doc.context("tokenizer").asInstanceOf[Tokenizer]
      doc.sentences.foreach(sentence => { 
        val phrases = tokenizer.phraseTokenize(sentence)
        phbuf ++= phrases
  	  })
  	  doc.copy(phrases=phbuf.toList)
    }
  }

  def calculateBaseScores = (doc: Doc) => {
    if (! shouldPerform(doc, "calculateBaseScores")) doc
    else {
      val baseScore = ScoreCalculator.baseScores(doc.phrases)
      doc.copy(scores=baseScore)
    }
  }
}