package com.healthline.scalcium.transformers

import com.healthline.scalcium.utils.Tokenizer
import com.healthline.scalcium.utils.ConfigUtils
import com.healthline.scalcium.utils.Logger

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
}