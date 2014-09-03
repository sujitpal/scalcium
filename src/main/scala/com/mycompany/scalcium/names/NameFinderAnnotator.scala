package com.mycompany.scalcium.pipeline

import org.apache.uima.jcas.JCas
import com.github.jenshaase.uimascala.core.SCasAnnotator_ImplBase
import com.mycompany.scalcium.names.OpenNLPNameFinder
import com.mycompany.scalcium.tokenizers.Tokenizer

class NameFinderAnnotator extends SCasAnnotator_ImplBase {

  val tokenizer = Tokenizer.getTokenizer("opennlp")
  val namefinder = new OpenNLPNameFinder()
  
  override def process(jcas: JCas): Unit = {
    val text = jcas.getDocumentText()
    val sentences = tokenizer.sentTokenize(text)
    val soffsets = sentences.map(sentence => sentence.length())
                            .scanLeft(0)(_ + _)
    // people annotations
    val allPersons = namefinder.find(namefinder.personME, sentences)
    applyAnnotations(jcas, allPersons, sentences, soffsets, "PER")
    // organization annotations
    val allOrgs = namefinder.find(namefinder.orgME, sentences)
    applyAnnotations(jcas, allOrgs, sentences, soffsets, "ORG")
  }
  
  def applyAnnotations(jcas: JCas, 
      allEnts: List[List[(String,Int,Int)]], sentences: List[String], 
      soffsets: List[Int], tag: String): Unit = {
    var sindex = 0
    allEnts.map(ents => { // all entities in each sentence
      ents.map(ent => {   // entity
        val coffset = charOffset(soffsets(sindex) + sindex,
          sentences(sindex), ent)
        val entity = new Entity(jcas, coffset._1, coffset._2)
        entity.setEntityType(tag)
        entity.addToIndexes()
      })
      sindex += 1
    })
  }

  def charOffset(soffset: Int, sentence: String, ent: (String,Int,Int)): 
      (Int,Int) = {
    val estring = tokenizer.wordTokenize(sentence)
      .slice(ent._2, ent._3)
      .mkString(" ")
    val cbegin = soffset + sentence.indexOf(estring)
    val cend = cbegin + estring.length()
    (cbegin, cend)
  }
}