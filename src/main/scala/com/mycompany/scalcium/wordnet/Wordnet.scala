package com.mycompany.scalcium.wordnet

import java.io.File
import java.io.FileInputStream
import scala.Array.canBuildFrom
import scala.collection.TraversableOnce.flattenTraversableOnce
import scala.collection.mutable.ArrayBuffer
import edu.cmu.lti.jawjaw.util.WordNetUtil
import edu.cmu.lti.lexical_db.NictWordNet
import edu.cmu.lti.lexical_db.data.Concept
import edu.cmu.lti.ws4j.RelatednessCalculator
import edu.cmu.lti.ws4j.impl.JiangConrath
import edu.cmu.lti.ws4j.impl.LeacockChodorow
import edu.cmu.lti.ws4j.impl.Lesk
import edu.cmu.lti.ws4j.impl.Lin
import edu.cmu.lti.ws4j.impl.Path
import edu.cmu.lti.ws4j.impl.Resnik
import edu.cmu.lti.ws4j.impl.WuPalmer
import net.didion.jwnl.JWNL
import net.didion.jwnl.data.IndexWord
import net.didion.jwnl.data.POS
import net.didion.jwnl.data.PointerType
import net.didion.jwnl.data.PointerUtils
import net.didion.jwnl.data.Synset
import net.didion.jwnl.data.Word
import net.didion.jwnl.data.list.PointerTargetNode
import net.didion.jwnl.data.list.PointerTargetNodeList
import net.didion.jwnl.dictionary.Dictionary
import scala.collection.JavaConversions._

class Wordnet(val wnConfig: File) {

  JWNL.initialize(new FileInputStream(wnConfig))
  val dict = Dictionary.getInstance()

  val lexdb = new NictWordNet()
  val Path_Similarity = new Path(lexdb)
  val LCH_Similarity = new LeacockChodorow(lexdb)
  val WUP_Similarity = new WuPalmer(lexdb)
  val RES_Similarity = new Resnik(lexdb)
  val JCN_Similarity = new JiangConrath(lexdb)
  val LIN_Similarity = new Lin(lexdb)
  val Lesk_Similarity = new Lesk(lexdb)
 
  def allSynsets(pos: POS): Stream[Synset] = 
    dict.getIndexWordIterator(pos)
      .map(iword => iword.asInstanceOf[IndexWord])
      .map(iword => iword.getSenses())
      .flatten
      .toStream
  
  def synsets(lemma: String): List[Synset] = {
    POS.getAllPOS()
      .map(pos => pos.asInstanceOf[POS])  
      .map(pos => synsets(lemma, pos))
      .flatten
      .toList
  }
  
  def synsets(lemma: String, pos: POS): List[Synset] = {
    val iword = dict.getIndexWord(pos, lemma)
    if (iword == null) List.empty[Synset]
    else iword.getSenses().toList
  }
  
  def synset(lemma: String, pos: POS, 
      sid: Int): Option[Synset] = {
    val iword = dict.getIndexWord(pos, lemma)
    if (iword != null) Some(iword.getSense(sid)) 
    else None
  }

  def lemmas(s: String): List[Word] = {
    synsets(s)
      .map(ss => lemmas(Some(ss)))
      .flatten
      .filter(w => w.getLemma().equals(s))
  }

  def lemmas(oss: Option[Synset]): List[Word] = {
    oss match {
      case Some(ss) => ss.getWords().toList
      case _ => List.empty[Word]
    }
  }
  
  def lemma(oss: Option[Synset], wid: Int): Option[Word] = {
    oss match {
      case Some(x) => Option(lemmas(oss)(wid))
      case None => None
    }
  }
  
  def lemma(oss: Option[Synset], lem: String): Option[Word] = {
    oss match {
      case Some(ss) => {
        val words = ss.getWords()
          .filter(w => lem.equals(w.getLemma()))
        if (words.size > 0) Some(words.head)
        else None
      }
      case None => None
    }
  }
  
  ////////////////// similarities /////////////////////
  
  def pathSimilarity(loss: Option[Synset], 
      ross: Option[Synset]): Double = 
    getPathSimilarity(loss, ross, Path_Similarity)
    
  def lchSimilarity(loss: Option[Synset],
      ross: Option[Synset]): Double =
    getPathSimilarity(loss, ross, LCH_Similarity)
  
  def wupSimilarity(loss: Option[Synset], 
      ross: Option[Synset]): Double = 
    getPathSimilarity(loss, ross, WUP_Similarity)
    
  // WS4j Information Content Finder (ICFinder) uses
  // SEMCOR, Resnik, JCN and Lin similarities are with
  // the SEMCOR corpus.
  def resSimilarity(loss: Option[Synset], 
      ross: Option[Synset]): Double =
    getPathSimilarity(loss, ross, RES_Similarity)
    
  def jcnSimilarity(loss: Option[Synset], 
      ross: Option[Synset]): Double = 
    getPathSimilarity(loss, ross, JCN_Similarity)
    
  def linSimilarity(loss: Option[Synset], 
      ross: Option[Synset]): Double =
    getPathSimilarity(loss, ross, LIN_Similarity)
  
  def leskSimilarity(loss: Option[Synset], 
      ross: Option[Synset]): Double = 
    getPathSimilarity(loss, ross, Lesk_Similarity)
    
  def getPathSimilarity(loss: Option[Synset],
      ross: Option[Synset],
      sim: RelatednessCalculator): Double = {
	val lconcept = getWS4jConcept(loss)
	val rconcept = getWS4jConcept(ross)
	if (lconcept == null || rconcept == null) 0.0D
	else sim.calcRelatednessOfSynset(lconcept, rconcept)
      .getScore()
  }
  
  def getWS4jConcept(oss: Option[Synset]): Concept = {
    oss match {
      case Some(ss) => {
        val pos = edu.cmu.lti.jawjaw.pobj.POS.valueOf(
          ss.getPOS().getKey())
        val synset = WordNetUtil.wordToSynsets(
          ss.getWord(0).getLemma(), pos)
          .head
        new Concept(synset.getSynset(), pos)
      }
      case _ => null
    }
  } 
  
  ////////////////// Morphy ///////////////////////////
  
  def morphy(s: String, pos: POS): String = {
    val bf = dict.getMorphologicalProcessor()
      .lookupBaseForm(pos, s)
    if (bf == null) "" else bf.getLemma()
  }
  
  def morphy(s: String): String = {
    val bases = POS.getAllPOS().map(pos =>
      morphy(s, pos.asInstanceOf[POS]))
    .filter(str => (! str.isEmpty()))
    .toSet
    if (bases.isEmpty) "" else bases.toList.head
  }
  
  ////////////////// Synset ///////////////////////////
  
  def lemmaNames(oss: Option[Synset]): List[String] = {
    oss match {
      case Some(ss) => ss.getWords()
        .map(word => word.getLemma())
        .toList
      case _ => List.empty[String]
    }
  }
  
  def definition(oss: Option[Synset]): String = {
    oss match {
      case Some(ss) => {
        ss.getGloss()
          .split(";")
          .filter(s => !isQuoted(s.trim))
          .mkString(";")
      }
      case _ => ""
    }
  }
  
  def examples(oss: Option[Synset]): List[String] = {
    oss match {
      case Some(ss) => {
        ss.getGloss()
          .split(";")
          .filter(s => isQuoted(s.trim))
          .map(s => s.trim())
          .toList
      }
      case _ => List.empty[String]
    }
  }
  
  def hyponyms(oss: Option[Synset]): List[Synset] =
    relatedSynsets(oss, PointerType.HYPONYM)

  def hypernyms(oss: Option[Synset]): List[Synset] = 
    relatedSynsets(oss, PointerType.HYPERNYM)
  
  def partMeronyms(oss: Option[Synset]): List[Synset] = 
    relatedSynsets(oss, PointerType.PART_MERONYM)
  
  def partHolonyms(oss: Option[Synset]): List[Synset] =
    relatedSynsets(oss, PointerType.PART_HOLONYM)
    
  def substanceMeronyms(oss: Option[Synset]): List[Synset] = 
    relatedSynsets(oss, PointerType.SUBSTANCE_MERONYM)
  
  def substanceHolonyms(oss: Option[Synset]): List[Synset] =
    relatedSynsets(oss, PointerType.SUBSTANCE_HOLONYM)
    
  def memberHolonyms(oss: Option[Synset]): List[Synset] = 
    relatedSynsets(oss, PointerType.MEMBER_HOLONYM)

  def entailments(oss: Option[Synset]): List[Synset] = 
    relatedSynsets(oss, PointerType.ENTAILMENT)
  
  def entailedBy(oss: Option[Synset]): List[Synset] = 
    relatedSynsets(oss, PointerType.ENTAILED_BY)

  def relatedSynsets(oss: Option[Synset], 
      ptr: PointerType): List[Synset] = {
    oss match {
      case Some(ss) => ss.getPointers(ptr)
        .map(ptr => ptr.getTarget().asInstanceOf[Synset])
        .toList
      case _ => List.empty[Synset]
    }
  }

  def hypernymPaths(oss: Option[Synset]): List[List[Synset]] = {
    oss match {
      case Some(ss) => PointerUtils.getInstance()
        .getHypernymTree(ss)
        .toList
        .map(x => x.asInstanceOf[PointerTargetNodeList])
        .map(ptnl => ptnl
          .map(x => x.asInstanceOf[PointerTargetNode])
          .map(ptn => ptn.getSynset())
          .toList)
        .toList
      case _ => List.empty[List[Synset]]
    }
  }
  
  def rootHypernyms(oss: Option[Synset]): List[Synset] = {
    hypernymPaths(oss)
      .map(hp => hp.reverse.head)
      .toSet
      .toList
  }
  
  def lowestCommonHypernym(loss: Option[Synset], 
      ross: Option[Synset]): List[Synset] = {
    val lpaths = hypernymPaths(loss)
    val rpaths = hypernymPaths(ross)
    val pairs = for (lpath <- lpaths; rpath <- rpaths) 
      yield (lpath, rpath)
    val lchs = ArrayBuffer[(Synset,Int)]()
    pairs.map(pair => {
      val lset = Set(pair._1).flatten
      val matched = pair._2
        .zipWithIndex
        .filter(si => lset.contains(si._1))
      if (! matched.isEmpty) lchs += matched.head
    })
    val lchss = lchs.sortWith((a, b) => a._2 < b._2)
      .map(lc => lc._1)  
      .toList
    if (lchss.isEmpty) List.empty[Synset] 
    else List(lchss.head)
  }
  
  def minDepth(oss: Option[Synset]): Int = {
    val lens = hypernymPaths(oss)
      .map(path => path.size)
      .sortWith((a,b) => a > b)
    if (lens.isEmpty) -1 else (lens.head - 1)
  }
  
  def format(ss: Synset): String = {
    List(ss.getWord(0).getLemma(), 
      ss.getPOS().getKey(),
      (ss.getWord(0).getIndex() + 1).formatted("%02d"))
      .mkString(".")
  }

  /////////////////// Words / Lemmas ////////////////////

  def antonyms(ow: Option[Word]): List[Word] = 
    relatedLemmas(ow, PointerType.ANTONYM)
  
  def relatedLemmas(ow: Option[Word], 
      ptr: PointerType): List[Word] = {
    ow match {
      case Some(w) => w.getPointers(ptr)
        .map(ptr => ptr.getTarget().asInstanceOf[Word])
        .toList
      case _ => List.empty[Word]
    }
  }
  
  def format(w : Word): String = {
    List(w.getSynset().getWord(0).getLemma(), 
      w.getPOS().getKey(),
      (w.getIndex() + 1).formatted("%02d"),
      w.getLemma())
      .mkString(".")
  }

  ////////////////// misc ////////////////////////////////
  
  def isQuoted(s: String): Boolean = {
    if (s.isEmpty()) false
    else (s.charAt(0) == '"' && 
      s.charAt(s.length() - 1) == '"')
  }
}
