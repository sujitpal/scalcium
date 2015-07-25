package com.mycompany.scalcium.sherlock

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

import scala.Array.canBuildFrom

import org.apache.commons.io.FileUtils

object SherlockMain extends App {

    val dataDir = new File("data/sherlock/")
    
    val metadataRemover = new MetadataRemover()
    val paragraphSplitter = new ParagraphSplitter()
    val sentenceSplitter = new SentenceSplitter()
    val nerFinder = new NERFinder()
    
    val outfile = new PrintWriter(
        new FileWriter(new File(dataDir, "output.tsv")), true)
    
    val gutenbergFiles = new File(dataDir, "gutenberg").listFiles()
    gutenbergFiles.zipWithIndex.map(fi => {
            val text = FileUtils.readFileToString(fi._1, "UTF-8")
            (fi._2, text)
        })
        .map(ft => (ft._1, metadataRemover.removeMetadata(ft._2)))
        .flatMap(ft => paragraphSplitter.split(ft))
        .flatMap(fpt => sentenceSplitter.split(fpt, false))
        .flatMap(fpst => nerFinder.find(fpst))
        .foreach(fpstt => save(outfile, fpstt.productIterator))

    outfile.flush()
    outfile.close()
        
    def save(outfile: PrintWriter, line: Iterator[_]): Unit = {
        outfile.println(line.toList.mkString("\t"))
    }
}
