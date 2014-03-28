package com.healthline.scalcium.utils

import scala.collection.mutable.ArrayBuffer

trait Guard[T] {
  def accept(token: T): Boolean
}

trait Action[T] {
  def perform(currState: String, token: T): Unit
}

class FSM[T](val debug: Boolean = false) {

  val states = ArrayBuffer[String]()
  val transitions = scala.collection.mutable.Map[
    String,ArrayBuffer[(String,Guard[T],Action[T])]]()
  var currState: String = "START"
  
  def addState(state: String): Unit = {
    states += state
  }
  
  def addTransition(from: String, to: String,
      guard: Guard[T], action: Action[T]): Unit = {
    val translist = transitions.getOrElse(from, ArrayBuffer()) 
    translist += ((to, guard, action))
    transitions.put(from, translist)
  } 
  
  def transition(token: T): Unit = {
    val targetStates = transitions.getOrElse(currState, List())
      .filter(tga => tga._2.accept(token))
    if (targetStates.size == 1) {
      val targetState = targetStates.head
      if (debug)
        Console.println("%s -> %s".format(currState, targetState._1))
      currState = targetState._1
      targetState._3.perform(currState, token)
    } else {
      transitions.get(currState) match {
        case Some(x) => x.head._3.perform(currState, token)
        case None => {}
      }
    }
  }
  
  def run(tokens: List[T]): Unit = tokens.foreach(transition(_))
}
