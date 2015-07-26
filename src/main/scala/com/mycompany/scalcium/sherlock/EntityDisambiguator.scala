package com.mycompany.scalcium.sherlock

import java.io.File
import scala.io.Source
import org.apache.commons.lang3.StringUtils

class EntityDisambiguator {

    def filterEntities(infile: File, entityType: String): List[String] = {
        Source.fromFile(infile).getLines
              .filter(line => line.split("\t")(4).equals(entityType))
              .map(line => line.split("\t")(3))
              .toList
    }
    
    def similarities(uniqueEntities: List[String]): 
            List[(String, String, Double)] = {
        val entityPairs = for (e1 <- uniqueEntities; 
                               e2 <- uniqueEntities)
                          yield (e1, e2)
        // the filter removes exact duplicates, eg (Holmes, Holmes)
        // as well as makes the LHS shorter than RHS, eg (Holmes,
        // Sherlock Holmes)
        entityPairs.filter(ee => ee._1.length < ee._2.length)
          .map(ee => (ee._1, ee._2, similarity(ee._1, ee._2)))
    }
    
    def similarity(entityPair: (String, String)): Double = {
        val levenshtein = StringUtils.getLevenshteinDistance(
            entityPair._1, entityPair._2)
        if (levenshtein == 0.0D) 1.0D
        else {
            val words1 = entityPair._1.split(" ").toSet
            val words2 = entityPair._2.split(" ").toSet
            words1.intersect(words2).size.toDouble / 
                ((words1.size + words2.size) * 0.5)
        }
    }
    
    def synonyms(sims: List[(String,String,Double)]): Map[String,String] = {
        sims.filter(ees => (!ees._1.equals(ees._2) && ees._3 > 0.6D))
            // remove duplicate mappings, eg Peter => Peter Carey and
            // Peter => Peter Jones. Like the unique cases which have 
            // no suspected candidates, these will resolve to themselves
            .groupBy(ee => ee._1)
            .filter(eeg => eeg._2.size == 1)
            .map(eeg => (eeg._1, eeg._2.head._2))
            .toMap
    }
}
