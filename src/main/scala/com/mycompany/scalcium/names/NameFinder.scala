package com.mycompany.scalcium.names

object NameFinder {

  def getNameFinder(name: String): NameFinder = 
    if ("opennlp".equals(name)) 
      new OpenNLPNameFinder()
    else new StanfordNameFinder()
}

trait NameFinder {
  
  def find(sentences: List[String]): List[List[(String,Int,Int)]]
}