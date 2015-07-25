package com.mycompany.scalcium.sherlock

import scala.Array.canBuildFrom
import scala.collection.mutable.Stack

class ParagraphSplitter {
    
    def split(fileText: (Int, String)): List[(Int, Int, String)] = {
        val filename = fileText._1
        val lines = fileText._2.split("\n")
        val nonEmptyLineNbrPairs = lines
            .zipWithIndex
            .filter(li => li._1.trim.size > 0)
            .map(li => li._2)
            .sliding(2)
            .map(poss => (poss(0), poss(1)))
            .toList
        val spans = Stack[(Int,Int)]()
        var inSpan = false
        nonEmptyLineNbrPairs.foreach(pair => {
            if (spans.isEmpty) {
                if (pair._1 + 1 == pair._2) {
                    spans.push(pair)
                    inSpan = true
                } else {
                    spans.push((pair._1, pair._1))
                    spans.push((pair._2, pair._2))
                }
            } else {
                val last = spans.pop
                if (pair._1 + 1 == pair._2) {
                    spans.push((last._1, pair._2))
                    inSpan = true
                } else {
                    if (inSpan) {
                        spans.push((last._1, pair._1 + 1))
                        spans.push((pair._2, pair._2))
                        inSpan = false
                    } else {
                        spans.push(last)
                        spans.push((pair._2, pair._2))
                    }
                }
            }
        })
        val lastSpan = spans.pop
        spans.push((lastSpan._1, lastSpan._2 + 1))
        
        // extract paragraphs in order and add extra sequence info
        spans.reverse.toList
             .map(span => {
                  if (span._1 == span._2) lines(span._1)
                  else lines.slice(span._1, span._2).mkString(" ")
             })
             .filter(line => !line.startsWith(" ") &&
                             line.length > 40) // remove TOCs and titles
            .zipWithIndex
            .map(paraIdx => (filename, paraIdx._2, paraIdx._1))
    }
}
