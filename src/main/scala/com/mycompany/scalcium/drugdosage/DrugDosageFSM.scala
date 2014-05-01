package com.mycompany.scalcium.drugdosage

import java.io.File
import java.util.regex.Pattern

import scala.collection.TraversableOnce.flattenTraversableOnce
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import com.mycompany.scalcium.utils.Action
import com.mycompany.scalcium.utils.FSM
import com.mycompany.scalcium.utils.Guard

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
    stab += ((token, currState))
  }
}

class DrugDosageFSM(val drugFile: File, 
    val freqFile: File, 
    val routeFile: File,
    val unitsFile: File,
    val numPatternsFile: File,
    val debug: Boolean = false) {

  def parse(s: String): List[(String,String)] = {
    val collector = new CollectAction(debug)
    val fsm = buildFSM(collector, debug)
    val x = fsm.run(s.toLowerCase()
        .replaceAll("[,;]", " ")
        .replaceAll("\\s+", " ")
        .split(" ")
        .toList)
    collector.stab.toList
  }
  
  def buildFSM(collector: CollectAction, 
      debug: Boolean): FSM[String] = {
    val fsm = new FSM[String](collector, debug)
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
    fsm.addTransition("START", "DRUG", drugGuard)
    fsm.addTransition("DRUG", "DOSAGE", dosageGuard)
    fsm.addTransition("DRUG", "FREQ", freqGuard)
    fsm.addTransition("DRUG", "ROUTE", routeGuard)
    fsm.addTransition("DOSAGE", "ROUTE", routeGuard)
    fsm.addTransition("DOSAGE", "FREQ", freqGuard)
    fsm.addTransition("ROUTE", "FREQ", freqGuard)
    fsm.addTransition("FREQ", "QTY", qtyGuard)
    fsm.addTransition("FREQ", "END", noGuard)
    fsm.addTransition("QTY", "REFILL", refillGuard)
    fsm.addTransition("QTY", "END", noGuard)
    fsm.addTransition("REFILL", "END", noGuard)
    
    fsm
  }
}  
  
