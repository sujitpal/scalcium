package com.healthline.scalcium.transformers

import java.io.File
import java.util.regex.Pattern

import scala.Array.canBuildFrom
import scala.io.Source

case class Offset(val start: Int, val end: Int) {
  def isNone = start == -1 && end == -1
  def None = Offset(-1,-1)
}

class NegexAnnotator(ruleFile: File, 
    responses: List[String]) {

  val rules = sortRules(ruleFile)
  
  /**
   * Predicts sense of the phrase in the sentence
   * as one of the responses based on whether the
   * negTagger method returns true or false.
   * @param sentence the sentence.
   * @param phrase the phrase.
   * @nonStrict true if non-strict mode.
   * @return a response (passed into constructor).
   */
  def predict(sentence: String, phrase: String, 
      nonStrict: Boolean): String = 
    if (negTagger(sentence, phrase, nonStrict)) 
      responses(0)
    else responses(1)

  /**
   * Parses trigger rules file and converts them
   * to a List of (trigger pattern, rule type) pairs
   * sorted by descending order of length of 
   * original trigger string. This method is called
   * on construction (should not be called from 
   * client code).
   * @param the trigger rules File.
   * @return List of (trigger pattern, rule type)
   *       sorted by trigger string length.
   */
  def sortRules(ruleFile: File): List[(Pattern,String)] = {
    Source.fromFile(ruleFile)
      .getLines()
      // input format: trigger phrase\t\t[TYPE]
      .map(line => {
        val cols = line.split("\t\t")
        (cols(0), cols(1))
      })
      .toList
      // sort by length descending
      .sortWith((a,b) => a._1.length > b._1.length)
      // replace spaces by \\s+ and convert to pattern
      .map(pair => (
        Pattern.compile("\\b(" + pair._1
          .trim()
          .replaceAll("\\s+", "\\\\s+") + ")\\b"), 
            pair._2))
  }
  
  /**
   * This is the heart of the algorithm. It normalizes
   * the incoming sentence, then finds the character
   * offset (start,end) for the phrase. If a CONJ
   * trigger is found, it only considers the part of
   * the sentence where the phrase was found. It 
   * looks at the PREN, POST, PREP and POSP (the
   * last 2 if tagPossible=true) looking for trigger
   * terms within 5 words of the phrase.
   * @param sentence the sentence (unnormalized).
   * @param phrase the phrase (unnormalized)
   * @param tagPossible true if non-strict mode
   *        annotation required.
   */
  def negTagger(sentence: String, phrase: String, 
      tagPossible: Boolean): Boolean = {
    val normSent = sentence.toLowerCase()
      .replaceAll("\\s+", " ")
    val wordPositions = 0 :: normSent.toCharArray()
      .zipWithIndex
      .filter(ci => ci._1 == ' ')
      .map(ci => ci._2 + 1)
      .toList
    // tag the phrase
    val phrasePattern = Pattern.compile(
      "\\b(" + 
      phrase.replaceAll("\\s+", "\\\\s+") + 
      ")\\b", Pattern.CASE_INSENSITIVE)
    val phraseOffset = offset(normSent, phrasePattern)
    if (phraseOffset.isNone) return false
    // look for CONJ trigger terms
    val conjOffsets = offsets(normSent, "[CONJ]", rules)
    if (conjOffsets.isEmpty) {
      // run through the different rule sets, 
      // terminating when we find a match
      val triggerTypes = if (tagPossible) 
        List("[PREN]", "[POST]", "[PREP]", "[POSP]")
      else List("[PREN]", "[POST]")
      isTriggerInScope(normSent, rules, 
        phraseOffset, wordPositions, triggerTypes)      
    } else {
      // chop off the side of the sentence where
      // the phrase does not appear.
      val conjOffset = conjOffsets.head
      if (conjOffset.end < phraseOffset.start) {
        val truncSent = normSent.substring(conjOffset.end + 1)
        negTagger(truncSent, phrase, tagPossible)
      } else if (phraseOffset.end < conjOffset.start) {
        val truncSent = normSent.substring(0, conjOffset.start)
        negTagger(truncSent, phrase, tagPossible)
      } else {
        false
      }
    }
  }
  
  /**
   * Returns true if the trigger term is within the
   * context of the phrase, ie, within 5 words of
   * each other. Recursively checks each rule type
   * in the triggerTypes in list.
   * @param sentence the normalized sentence.
   * @param rules the sorted list of rules.
   * @param phraseOffset the phrase offset.
   * @param wordPositions the positions of the
   *        starting character position of each
   *        word in the normalized sentence.
   * @param triggerTypes the trigger types to
   *        check.
   * @return true if trigger is in the context of
   *        the phrase, false if not.
   */
  def isTriggerInScope(
      sentence: String,
      rules: List[(Pattern,String)],
      phraseOffset: Offset,
      wordPositions: List[Int],
      triggerTypes: List[String]): Boolean = {
    if (triggerTypes.isEmpty) false
    else {
      val currentTriggerType = triggerTypes.head
      val triggerOffsets = offsets(sentence, 
        currentTriggerType, rules)
      val selectedTriggerOffset = firstNonOverlappingOffset(
        phraseOffset, triggerOffsets)
      if (selectedTriggerOffset.isNone)
        // try with the next trigger pattern
        isTriggerInScope(sentence, rules, 
          phraseOffset, wordPositions,
          triggerTypes.tail)
      else {
        // check how far the tokens are. If PRE*
        // token, then there is no distance limit
        // but 5 words is the distance limit for 
        // POS* rules.
        if (currentTriggerType.startsWith("[PRE"))
          selectedTriggerOffset.start < 
            phraseOffset.start
        else
          wordDistance(phraseOffset, 
            selectedTriggerOffset, 
            wordPositions) <= 5 &&
            phraseOffset.start < 
            selectedTriggerOffset.start
      }
    }
  }
  
  /**
   * Returns the distance in number of words
   * between the phrase and trigger term.
   * @param phraseOffset (start,end) for phrase.
   * @param triggerOffset (start,end) for trigger.
   * @param wordPositions a list of starting
   *        character positions for each word
   *        in (normalized) sentence.
   * @return number words between phrase and trigger.
   */
  def wordDistance(phraseOffset: Offset, 
      triggerOffset: Offset,
      wordPositions: List[Int]): Int = {
    if (phraseOffset.start < triggerOffset.start)
      wordPositions
        .filter(pos => pos > phraseOffset.end && 
          pos < triggerOffset.start)
        .size
    else
      wordPositions
        .filter(pos => pos > triggerOffset.end && 
          pos < phraseOffset.start)
        .size
  }
  
  /**
   * Compute the character offset of the phrase
   * in the (normalized) sentence. If there is 
   * no match, then an Offset(-1,-1) is returned.
   * @param sentence the normalized sentence.
   * @param pattern the phras 
   */
  def offset(sentence: String, 
      pattern: Pattern): Offset = {
    val matcher = pattern.matcher(sentence)
    if (matcher.find()) 
      Offset(matcher.start(), matcher.end())
    else Offset(-1, -1)      
  }
  
  /**
   * Find all offsets for trigger terms for the
   * specified rule type. Returns a list of offsets
   * for trigger terms that matched.
   * @param sentence the normalized sentence.
   * @param ruleType the rule type to filter on.
   * @param rules the list of sorted rule patterns.
   * @return a List of Offsets for matched triggers
   *        of the specified rule type.
   */
  def offsets(sentence: String, ruleType: String,
      rules: List[(Pattern,String)]): List[Offset] = {
    rules.filter(rule => ruleType.equals(rule._2))
      .map(rule => offset(sentence, rule._1))
      .filter(offset => (! offset.isNone))
  }
  
  /**
   * Returns the first trigger term that does not
   * overlap with the phrase. May return (-1,-1).
   * @param phraseOffset the offset for the phrase.
   * @param triggerOffsets a list of Offsets for the
   *        triggers.
   * @return the first non-overlapping offset.
   */
  def firstNonOverlappingOffset(phraseOffset: Offset, 
      triggerOffsets: List[Offset]): Offset = {
    val phraseRange = Range(phraseOffset.start, phraseOffset.end)
    val nonOverlaps = triggerOffsets
      .filter(offset => {
        val offsetRange = Range(offset.start, offset.end)  
        phraseRange.intersect(offsetRange).size == 0
      })
    if (nonOverlaps.isEmpty) Offset(-1,-1) 
    else nonOverlaps.head
  }
}