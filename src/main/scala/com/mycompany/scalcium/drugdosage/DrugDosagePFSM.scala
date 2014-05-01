package com.mycompany.scalcium.drugdosage

import com.mycompany.scalcium.utils.PFSM
import java.io.File

class DrugDosagePFSM(val drugFile: File, 
    val freqFile: File, val routeFile: File,
    val unitsFile: File, val numPatternsFile: File,
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
      debug: Boolean): PFSM[String] = {
    val pfsm = new PFSM[String](collector, debug)
    
    pfsm.addState("START")
    pfsm.addState("DRUG")
    pfsm.addState("FREQ")
    pfsm.addState("NUM")
    pfsm.addState("ROUTE")
    pfsm.addState("UNIT")
    pfsm.addState("END")
    
    val noGuard = new BoolGuard(false)
    val drugGuard = new DictGuard(drugFile)
    val freqGuard = new DictGuard(freqFile)
    val routeGuard = new DictGuard(routeFile)
    val unitsGuard = new DictGuard(unitsFile)
    val numGuard = new RegexGuard(numPatternsFile)
    
    pfsm.addTransition("START", "DRUG", 1.0, drugGuard)
    
    pfsm.addTransition("DRUG", "FREQ", 0.01, freqGuard)
    pfsm.addTransition("DRUG", "NUM", 0.67, numGuard)
    pfsm.addTransition("DRUG", "ROUTE", 0.02, routeGuard)

    pfsm.addTransition("FREQ", "NUM", 0.12, numGuard)
    
    pfsm.addTransition("NUM", "FREQ", 0.12, freqGuard)
    pfsm.addTransition("NUM", "ROUTE", 0.04, routeGuard)
    pfsm.addTransition("NUM", "UNIT", 0.71, unitsGuard)
    
    pfsm.addTransition("ROUTE", "FREQ", 0.89, freqGuard)

    pfsm.addTransition("UNIT", "FREQ", 0.16, freqGuard)
    pfsm.addTransition("UNIT", "NUM", 0.16, numGuard)
    pfsm.addTransition("UNIT", "ROUTE", 0.54, routeGuard)

    pfsm.addTransition("FREQ", "END", 0.25, noGuard)
    pfsm.addTransition("NUM", "END", 0.25, noGuard)
    pfsm.addTransition("UNIT", "END", 0.25, noGuard)
    pfsm.addTransition("ROUTE", "END", 0.25, noGuard)
    
    pfsm
  }

}
