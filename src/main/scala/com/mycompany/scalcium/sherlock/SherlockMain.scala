package com.mycompany.scalcium.sherlock

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

import scala.Array.canBuildFrom
import scala.collection.mutable.ArrayBuffer

import org.apache.commons.io.FileUtils

object SherlockMain extends App {

    val dataDir = new File("data/sherlock/")
    
    val metadataRemover = new MetadataRemover()
    val paragraphSplitter = new ParagraphSplitter()
    val sentenceSplitter = new SentenceSplitter()
    val nerFinder = new NERFinder()
    
    preprocess()
    wordCounts()
    
    
    def preprocess(): Unit = {
        val outfile = new PrintWriter(
            new FileWriter(new File(dataDir, "output.tsv")), true)
        val gutenbergFiles = new File(dataDir, "gutenberg").listFiles()
        gutenbergFiles.zipWithIndex.map(tf => {
                val text = FileUtils.readFileToString(tf._1, "UTF-8")
                (tf._2, text)
            })
            .map(ft => (ft._1, metadataRemover.removeMetadata(ft._2)))
            .flatMap(ft => paragraphSplitter.split(ft))
            .flatMap(fpt => sentenceSplitter.split(fpt, false))
            .flatMap(fpst => nerFinder.find(fpst))
            .foreach(fpstt => save(outfile, fpstt.productIterator))
        outfile.flush()
        outfile.close()
    }
        
    def save(outfile: PrintWriter, line: Iterator[_]): Unit = {
        outfile.println(line.toList.mkString("\t"))
    }

    def wordCounts(): Unit = {
        val gutenbergFiles = new File(dataDir, "gutenberg").listFiles()
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
    
    def mean(xs: Array[Int]): Double = {
        val sum = xs.foldLeft(0)(_ + _)
        sum.toDouble / xs.size
    }
}
