package com.mycompany.scalcium.names

object NameFinder {

  def getNameFinder(name: String): NameFinder = { 
    name.toLowerCase() match {
      case "opennlp" => new OpenNLPNameFinder()
      case "stanford" => new StanfordNameFinder()
    }
  }
}

trait NameFinder {
  
  /**
   * Finds named entities (of various types, depending on the 
   * extractor used) from a List of sentences and returns a List
   * of List of Triples of entity type, start and end character
   * offset for each entity found. Each sublist corresponds to 
   * the sentence from which the extraction occurred.
   * @param sentences a List of sentences.
   * @return a List of List of triples (entity type, start char
   *         offset, end char offset).
   */
  def find(sentences: List[String]): List[List[(String,Int,Int)]]
}