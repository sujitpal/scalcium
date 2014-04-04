package com.healthline.scalcium.utils

import scala.collection.mutable.ArrayBuffer

trait Guard[T] {
  def accept(token: T): Boolean
}

trait Action[T] {
  def perform(currState: String, token: T): Unit
}

class FSM[T](val action: Action[T],
    val debug: Boolean = false) {

  val states = ArrayBuffer[String]()
  val transitions = scala.collection.mutable.Map[
    String,ArrayBuffer[(String,Guard[T])]]()
  var currState: String = "START"
  
  def addState(state: String): Unit = {
    states += state
  }
  
  def addTransition(from: String, to: String,
      guard: Guard[T]): Unit = {
    val translist = transitions.getOrElse(from, ArrayBuffer()) 
    translist += ((to, guard))
    transitions(from) = translist
  } 
  
  def transition(token: T): Unit = {
    val tgas = transitions.getOrElse(currState, List())
      .filter(tga => tga._2.accept(token))
    if (tgas.size == 1) {
      // no ambiguity, just take the path specified
      val tga = tgas.head
      if (debug)
        Console.println("%s -> %s".format(currState, tga._1))
      currState = tga._1
      action.perform(currState, token)
    } else {
      if (tgas.isEmpty) 
        action.perform(currState, token) 
      else {
        currState = tgas.head._1
        action.perform(currState, token)
      }
    }
  }
  
  def run(tokens: List[T]): Unit = tokens.foreach(transition(_))
}

class PFSM[T](action: Action[T], debug: Boolean = false) 
    extends FSM[T](action, debug) {

  val tprobs = scala.collection.mutable.Map[
    ((String,String)),Double]()

  def addTransition(from: String, to: String,
      tprob: Double, guard: Guard[T]): Unit = {
    super.addTransition(from, to, guard)
    tprobs((from, to)) = tprob
  }

  override def transition(token: T): Unit = {
    val tgas = transitions.getOrElse(currState, List())
      .filter(tga => tga._2.accept(token))
    if (tgas.size == 1) {
      // no ambiguity, just take the path specified
      val tga = tgas.head
      if (debug)
        Console.println("%s -> %s".format(currState, tga._1))
      currState = tga._1
      action.perform(currState, token)
    } else {
      // choose the most probable transition based
      // on tprobs. Break ties by choosing head as before
      if (tgas.isEmpty) 
        action.perform(currState, token) 
      else {
        val bestTga = tgas
          .map(tga => (tga, tprobs((currState, tga._1))))
          .sortWith((a, b) => a._2 > b._2)
          .head._1
        currState = bestTga._1
        action.perform(currState, token)
      }
    }
  }
}
