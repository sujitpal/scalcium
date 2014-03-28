package com.healthline.scalcium.drugdosage

import scala.collection.mutable.ArrayBuffer

import com.healthline.scalcium.utils.Action
import com.healthline.scalcium.utils.FSM
import com.healthline.scalcium.utils.Guard

//class EqualsGuard(val refValue: String) extends Guard[String] {
//  override def accept(token: String): Boolean = 
//    if (refValue.equals(token)) true else false
//}

//class BoolGuard(val refValue: Boolean) extends Guard[String] {
//  override def accept(token: String): Boolean = refValue
//}

//class CollectAction(var coll: Map[String,ArrayBuffer[String]]) 
//    extends Action[String] {
//  override def perform(currState: String, token: String): Unit = {
//    Console.println("setting: %s to %s".format(token, currState))
//    val collval = coll.getOrElse(currState, ArrayBuffer())
//    collval += token
//    coll = coll + ((currState, collval))
//  }
//}

//class SqlParser {
//  
//  val s = "SELECT a, b FROM table WHERE a > 5 ORDER BY b;"
//  
//  val fsm = new FSM[String](debug=true)
//  fsm.addState("START")
//  fsm.addState("SELECT")
//  fsm.addState("FROM")
//  fsm.addState("WHERE")
//  fsm.addState("ORDER")
//  fsm.addState("END")
//  
//  val selectGuard = new EqualsGuard("SELECT")
//  val fromGuard = new EqualsGuard("FROM")
//  val whereGuard = new EqualsGuard("WHERE")
//  val orderGuard = new EqualsGuard("ORDER")
//  val noGuard = new BoolGuard(false)
//  
//  val collectAction = new CollectAction(
//    Map[String,ArrayBuffer[String]]())
//  
//  fsm.addTransition("START", "SELECT", selectGuard, collectAction)
//  fsm.addTransition("SELECT", "FROM", fromGuard, collectAction)
//  fsm.addTransition("FROM", "WHERE", whereGuard, collectAction)
//  fsm.addTransition("WHERE", "ORDER", orderGuard, collectAction)
//  fsm.addTransition("ORDER", "END", noGuard, collectAction)
//
//  fsm.run(s.split(" ").toList)
//  Console.println("coll=" + collectAction.coll)
//}
