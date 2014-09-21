package com.mycompany.scalcium.coref

object CorefResolver {
  def getResolver(name: String): CorefResolver = {
    name.toLowerCase() match {
      case "stanford" => new StanfordCorefResolver()
      case "lingpipe" => new LingPipeCorefResolver()
      case "opennlp" => new OpenNLPCorefResolver()
    }
  }
}

trait CorefResolver {
  def resolve(text: String): List[(CorefTriple,List[CorefTriple])]
}

case class CorefTriple(text: String, begin: Int, end: Int)
