package com.healthline.scalcium.drugdosage

import java.io.File
import java.util.regex.Pattern

import scala.collection.TraversableOnce.flattenTraversableOnce
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import com.healthline.scalcium.utils.Action
import com.healthline.scalcium.utils.FSM
import com.healthline.scalcium.utils.Guard

class BoolGuard(val refValue: Boolean) extends Guard[String] {
  override def accept(token: String): Boolean = refValue
}

class DictGuard(val file: File) extends Guard[String] {
  val words = Source.fromFile(file).getLines()
    .map(line => line.toLowerCase().split(" ").toList)
    .flatten
    .toSet
  
  override def accept(token: String): Boolean = {
    words.contains(token.toLowerCase())
  }
}

class RegexGuard(val file: File) extends Guard[String] {
  val patterns = Source.fromFile(file).getLines()
    .map(line => Pattern.compile(line))
    .toList
  
  override def accept(token: String): Boolean = {
    patterns.map(pattern => {
      val matcher = pattern.matcher(token)
      if (matcher.matches()) return true
    })
    false
  }
}

class CombinedGuard(val file: File, val pfile: File) 
      extends Guard[String] {
  val words = Source.fromFile(file).getLines()
    .map(line => line.toLowerCase().split(" ").toList)
    .flatten
    .toSet
  val patterns = Source.fromFile(pfile).getLines()
    .map(line => Pattern.compile(line))
    .toList
    
  override def accept(token: String): Boolean = {
    acceptWord(token) || acceptPattern(token)
  }
  
  def acceptWord(token: String): Boolean = 
    words.contains(token.toLowerCase())
    
  def acceptPattern(token: String): Boolean = {
    patterns.map(pattern => {
      val matcher = pattern.matcher(token)
      if (matcher.matches()) return true
    })
    false
  }
}

class CollectAction(val debug: Boolean) extends Action[String] {
  val stab = new ArrayBuffer[(String,String)]()
  
  override def perform(currState: String, token: String): Unit = {
    if (debug)
      Console.println("setting: %s to %s".format(token, currState))
    stab += ((currState, token))
  }
  
  def getSymbolTable(): Map[String,List[String]] = {
    stab.groupBy(kv => kv._1)
      .map(kv => (kv._1, kv._2.map(_._2).toList))
  }
}

class DrugDosageFSM(val drugFile: File, 
    val freqFile: File, 
    val routeFile: File,
    val unitsFile: File,
    val numPatternsFile: File,
    val debug: Boolean = false) {

  
  def parse(s: String): Map[String,List[String]] = {
    val collector = new CollectAction(debug)
    val fsm = buildFSM(collector, debug)
    val x = fsm.run(s.toLowerCase()
        .replaceAll("[,;]", " ")
        .replaceAll("\\s+", " ")
        .split(" ")
        .toList)
    collector.getSymbolTable()
  }
  
  def buildFSM(collector: CollectAction, debug: Boolean): FSM[String] = {
    val fsm = new FSM[String](debug)
    // states
    fsm.addState("START")
    fsm.addState("DRUG")
    fsm.addState("DOSAGE")
    fsm.addState("ROUTE")
    fsm.addState("FREQ")
    fsm.addState("QTY")
    fsm.addState("REFILL")
    fsm.addState("END")
  
    val noGuard = new BoolGuard(false)
    val drugGuard = new DictGuard(drugFile)
    val dosageGuard = new CombinedGuard(unitsFile, numPatternsFile)
    val freqGuard = new DictGuard(freqFile)
    val qtyGuard = new RegexGuard(numPatternsFile)
    val routeGuard = new DictGuard(routeFile)
    val refillGuard = new CombinedGuard(unitsFile, numPatternsFile)
    
    // transitions
    fsm.addTransition("START", "DRUG", drugGuard, collector)
    fsm.addTransition("DRUG", "DOSAGE", dosageGuard, collector)
    fsm.addTransition("DRUG", "FREQ", freqGuard, collector)
    fsm.addTransition("DRUG", "ROUTE", routeGuard, collector)
    fsm.addTransition("DOSAGE", "ROUTE", routeGuard, collector)
    fsm.addTransition("DOSAGE", "FREQ", freqGuard, collector)
    fsm.addTransition("ROUTE", "FREQ", freqGuard, collector)
    fsm.addTransition("FREQ", "QTY", qtyGuard, collector)
    fsm.addTransition("FREQ", "END", noGuard, collector)
    fsm.addTransition("QTY", "REFILL", refillGuard, collector)
    fsm.addTransition("QTY", "END", noGuard, collector)
    fsm.addTransition("REFILL", "END", noGuard, collector)
    
    fsm
  }
}  
  