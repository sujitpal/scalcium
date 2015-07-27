package com.mycompany.scalcium.sherlock

import java.io.File
import scala.io.Source
import java.io.PrintWriter
import java.io.FileWriter

class GraphDataGenerator {

    def pFile = 1.0D / 30579
    def pPara = 1.0D / 56.534
    def pSent = 1.0D / 15.629
    
    def vertices(infile: File, minFreq: Int = 0): Map[String, Int] = {
        Source.fromFile(infile).getLines
              .map(line => line.split("\t")(3))
              .toList
              .groupBy(name => name)
              .map(ng => (ng._1, ng._2.size))
              .filter(ns => ns._2 > minFreq)
    }
    
    def edges(infile: File, minSim: Double = 0.01D, 
            exclude: Set[String] = Set()): 
            List[(String,String,Double)] = {
        val personData = Source.fromFile(infile).getLines
                               .map(line => line.split("\t"))
                               .filter(cols => !exclude.contains(cols(3)))
                               .toList
        val personPairs = for (p1 <- personData; p2 <- personData) 
                          yield (p1, p2)
        personPairs.filter(pp => pp._1(3).length < pp._2(3).length)
            .map(pp => (pp._1(3), pp._2(3), edgeWeight(pp._1, pp._2)))
            .toList
            .groupBy(ppw => (ppw._1, ppw._2))
            .map(ppwg => (ppwg._1._1, ppwg._1._2, ppwg._2.map(_._3).sum))
            .filter(ppwg => ppwg._3 > minSim)
            .toList
    }
    
    def edgeWeight(p1: Array[_], p2: Array[_]): Double = {
        val p1Locs = p1.slice(0, 3).map(_.asInstanceOf[String].toInt)
        val p2Locs = p2.slice(0, 3).map(_.asInstanceOf[String].toInt)
        if (p1Locs(0) == p2Locs(0)) { // same file
            if (p1Locs(1) == p2Locs(1)) { // same para
                if (p1Locs(2) == p2Locs(2)) pSent // same sentence
                else pPara // same para but not same sentence 
            } else pFile // same file but not same para
        } else 0.0D // not same file
    }
}
