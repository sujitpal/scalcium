package com.mycompany.scalcium.utils

import scala.collection.mutable.ArrayBuffer

object NGram {

  def bigrams(tokens: List[Any]): List[List[Any]] = ngrams(tokens, 2)
  
  def trigrams(tokens: List[Any]): List[List[Any]] = ngrams(tokens, 3)
  
  def ngrams(tokens: List[Any], n: Int): List[List[Any]] = {
    val nwords = tokens.size
    val ngrams = new ArrayBuffer[List[Any]]()
    for (i <- 0 to (nwords - n)) {
      ngrams += tokens.slice(i, i + n)
    }
    ngrams.toList
  }
}
