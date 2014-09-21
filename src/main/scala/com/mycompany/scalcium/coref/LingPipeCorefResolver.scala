package com.mycompany.scalcium.coref

import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import scala.collection.JavaConversions._
import scala.util.matching.Regex
import com.aliasi.chunk.Chunk
import com.aliasi.chunk.ChunkFactory
import com.aliasi.chunk.Chunker
import com.aliasi.coref.EnglishMentionFactory
import com.aliasi.coref.WithinDocCoref
import com.aliasi.sentences.MedlineSentenceModel
import com.aliasi.sentences.SentenceChunker
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory
import com.aliasi.util.Streams
import com.aliasi.coref.Tags

class LingPipeCorefResolver extends CorefResolver {

  val ModelDir = "src/main/resources/lingpipe/models"
  val ChunkerModelFile = "ne-en-news-muc6.AbstractCharLmRescoringChunker"
  val MalePronouns = "(?i)\\b(he|him|his)\\b".r
  val FemalePronouns = "(?i)\\b(she|her|hers)\\b".r
  val NeuterPronouns = "(?i)\\b(it)\\b".r
    
  val tokenizerFactory = new IndoEuropeanTokenizerFactory()
  val sentenceModel = new MedlineSentenceModel()
  val sentenceChunker = new SentenceChunker(tokenizerFactory, sentenceModel)
  val entityChunker = readObject(new File(ModelDir, ChunkerModelFile))
    .asInstanceOf[Chunker]
    
  override def resolve(text: String): List[(CorefTriple,List[CorefTriple])] = {
	val mentionFactory = new EnglishMentionFactory()
    val coref = new WithinDocCoref(mentionFactory)
    val sentenceChunking = sentenceChunker.chunk(text.toCharArray, 0, text.length)
    val mentions = sentenceChunking.chunkSet()
      .zipWithIndex
      .map(chunkIndexPair => {
        val schunk = chunkIndexPair._1
        val sentence = text.substring(schunk.start, schunk.end)  
        // find entities in sentence
        val mentionChunking = entityChunker.chunk(sentence)
        val mentions = mentionChunking.chunkSet().toSet
        // add different types of pronoun entities
        val malePronouns = buildPronounMentions(
          MalePronouns, sentence, "MALE_PRONOUN", mentions)
        val femalePronouns = buildPronounMentions(
          FemalePronouns, sentence, "FEMALE_PRONOUN", mentions)
        val neuterPronouns = buildPronounMentions(
          NeuterPronouns, sentence, "NEUTER_PRONOUN", mentions)
        val allMentions = 
          mentions ++ malePronouns ++ femalePronouns ++ neuterPronouns
        // resolve coreferences
        allMentions.map(chunk => {
          val chstart = chunk.start
          val chend = chunk.end
          val chtext = sentence.substring(chstart, chend)
          val chtype = chunk.`type`
          val mention = mentionFactory.create(chtext, chtype)
          val mentionId = coref.resolveMention(mention, chunkIndexPair._2)
          (mentionId, (schunk.start + chstart, schunk.start + chend, chtext))
        })
      })
      .flatten
      .groupBy(pair => pair._1) // {mentionId => Set((mentionId, (chunk))
      .filter(kv => kv._2.size > 1) // filter out single mentions
      .map(kv => kv._2.map(x => CorefTriple(x._2._3, x._2._1, x._2._2)).toList)
      .toList                   // List[List[CorefTriple]]
    mentions.map(mention => {
      val head = mention.head
      val rest = mention.tail
      (head, rest)
    })
  }
  
  def readObject(f: File): Object = {
    val oistream = new ObjectInputStream(new FileInputStream(f))
    val obj = oistream.readObject
    Streams.closeQuietly(oistream)
    obj
  }
  
  def buildPronounMentions(regex: Regex, sentence: String, tag: String, 
      mentions: Set[Chunk]): Set[Chunk] =
    regex.findAllMatchIn(sentence)
      .map(m => ChunkFactory.createChunk(m.start, m.end, tag))
      .filter(pronoun => ! overlaps(mentions, pronoun))
      .toSet
  
  def overlaps(mentions: Set[Chunk], pronoun: Chunk): Boolean = {
    val pstart = pronoun.start
    val pend = pronoun.end
    mentions.filter(mention => {
      val maxStart = if (mention.start < pstart) pstart else mention.start
      val minEnd = if (mention.end < pend) mention.end else pend
      maxStart < minEnd
    })
    .size() > 0
  }
}