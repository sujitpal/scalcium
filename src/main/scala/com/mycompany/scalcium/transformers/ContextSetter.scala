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
}
