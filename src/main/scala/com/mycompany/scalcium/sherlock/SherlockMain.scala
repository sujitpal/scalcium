package com.mycompany.scalcium.sherlock

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

import scala.Array.canBuildFrom
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import org.apache.commons.io.FileUtils

object SherlockMain extends App {

    val dataDir = new File("data/sherlock/")
    
    val metadataRemover = new MetadataRemover()
    val paragraphSplitter = new ParagraphSplitter()
    val sentenceSplitter = new SentenceSplitter()
    val nerFinder = new NERFinder()
    val entityDisambiguator = new EntityDisambiguator()
    val graphDataGenerator = new GraphDataGenerator()
    
    preprocess(new File(dataDir, "gutenberg"), 
        new File(dataDir, "output.tsv"))
    wordCounts(new File(dataDir, "gutenberg"))
    disambiguatePersons(new File(dataDir, "output.tsv"), 
        new File(dataDir, "output-PER-disambig.tsv"))
    generateGraphData(new File(dataDir, "output-PER-disambig.tsv"),
        new File(dataDir, "PER-graph-verts.tsv"),
        new File(dataDir, "PER-graph-edges.tsv"))
        
    
    def preprocess(indir: File, outfile: File): Unit = {
        val writer = new PrintWriter(new FileWriter(outfile), true)
        val gutenbergFiles = indir.listFiles()
        gutenbergFiles.zipWithIndex.map(tf => {
                val text = FileUtils.readFileToString(tf._1, "UTF-8")
                (tf._2, text)
            })
            .map(ft => (ft._1, metadataRemover.removeMetadata(ft._2)))
            .flatMap(ft => paragraphSplitter.split(ft))
            .flatMap(fpt => sentenceSplitter.split(fpt, false))
            .flatMap(fpst => nerFinder.find(fpst))
            .foreach(fpstt => save(writer, fpstt.productIterator))
        writer.flush()
        writer.close()
    }
        
    def save(outfile: PrintWriter, line: Iterator[_]): Unit =
        outfile.println(line.toList.mkString("\t"))

    def wordCounts(indir: File): Unit = {
        val gutenbergFiles = indir.listFiles()
        val numWordsInFile = ArrayBuffer[Int]()
        val numWordsInPara = ArrayBuffer[Int]()
        val numWordsInSent = ArrayBuffer[Int]()
        gutenbergFiles.zipWithIndex.foreach(tf => {
            val text = metadataRemover.removeMetadata(
                FileUtils.readFileToString(tf._1, "UTF-8"))
            numWordsInFile += text.split(" ").size
            val paras = paragraphSplitter.split((tf._2, text))
            paras.foreach(para => {
                numWordsInPara += para._3.split(" ").size
                val sents = sentenceSplitter.split(para, false)
                sents.foreach(sent => 
                    numWordsInSent += sent._4.split(" ").size)
            })
        })
        Console.println("Average words/file: %.3f".format(
            mean(numWordsInFile.toArray)))                
        Console.println("Average words/para: %.3f".format(
            mean(numWordsInPara.toArray)))
        Console.println("Average words/sent: %.3f".format(
            mean(numWordsInSent.toArray)))
    }
    
    def mean(xs: Array[Int]): Double = 
        xs.foldLeft(0)(_ + _).toDouble / xs.size

    def disambiguatePersons(infile: File, outfile: File): Unit = {
        val personEntities = entityDisambiguator.filterEntities(
            new File(dataDir, "output.tsv"), "PERSON")
        val uniquePersonEntities = personEntities.distinct
        val personSims = entityDisambiguator.similarities(uniquePersonEntities)
        val personSyns = entityDisambiguator.synonyms(personSims, 0.6) ++
            // add few well-known ones that escaped the 0.6 dragnet
            Map(("Holmes", "Sherlock Holmes"),
                ("MR. HOLMES", "Sherlock Holmes"),
                ("MR. SHERLOCK HOLMES", "Sherlock Holmes"),
                ("Mycroft", "Mycroft Holmes"),
                ("Brother Mycroft", "Mycroft Holmes"))
        val writer = new PrintWriter(new FileWriter(outfile), true)
        Source.fromFile(infile).getLines
              .map(line => line.split("\t"))
              .filter(cols => cols(4).equals("PERSON"))
              .map(cols => (cols(0), cols(1), cols(2), 
                  personSyns.getOrElse(cols(3), cols(3))))
              .foreach(cs => writer.println("%d\t%d\t%d\t%s".format(
                  cs._1.toInt, cs._2.toInt, cs._3.toInt, cs._4)))
        writer.flush()
        writer.close()
    }
    
    def generateGraphData(infile: File, vfile: File, efile: File): Unit = {
        val vwriter = new PrintWriter(new FileWriter(vfile), true)
        val vertices = graphDataGenerator.vertices(infile, 0)
        vertices.toList.sortWith((a, b) => a._2 > b._2)
            .foreach(vert => vwriter.println("%s\t%d".format(
                vert._1, vert._2)))
        vwriter.flush()
        vwriter.close()
        // consider minFreq = 32 (top 20), create exclude Set
        val excludeVertices = vertices.filter(vert => vert._2 < 32)
                                      .map(vert => vert._1)
                                      .toSet
        val ewriter = new PrintWriter(new FileWriter(efile), true)
        val edges = graphDataGenerator.edges(infile, 0.1D, excludeVertices)
        edges.foreach(edge => ewriter.println("%s\t%s\t%.3f".format(
            edge._1, edge._2, edge._3)))
        ewriter.flush()
        ewriter.close()
    }
}
