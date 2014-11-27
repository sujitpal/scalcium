package com.mycompany.scalcium.transformers

import com.mycompany.scalcium.utils.ConfigUtils
import com.mycompany.scalcium.utils.Logger
import com.mycompany.scalcium.tokenizers.Tokenizer

/**
 * Sets the document with objects it will need to carry
 * through the pipeline.
 */
object ContextSetter {

  def setContext(doc: Doc): Map[String,Any] = {
    Map(
      ("tokenizer" -> Tokenizer.getTokenizer(
        ConfigUtils.getStringValue(doc.contentType, 
        "tokenizer", "opennlp"))),
      ("logger" -> Logger.getLogger(
        ConfigUtils.getStringValue(doc.contentType, 
        "logger", "none")))
    )
  }
  
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
  scores: Map[String,Float],
  context: Map[String,Any]
)
}
