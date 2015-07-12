package com.mycompany.scalcium.keyextract

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

import scala.Array.canBuildFrom
import scala.io.Source

import com.mycompany.scalcium.utils.DictNER

object BagOfKeywords extends App {

	val keaDir = "/Users/palsujit/Projects/med_data/mtcrawler/kea"
	val outDir = "/Users/palsujit/Projects/med_data/mtcrawler/kea_keys"
	
	val mergedKeys = new File(keaDir, "merged_keys.txt")
	val bok = new BagOfKeywords(mergedKeys)
	
	val files = new File(keaDir, "train").listFiles() ++
	            new File(keaDir, "test").listFiles()
	files.filter(file => file.getName().endsWith(".txt"))
         .map(file => {
        	 val keywords = bok.tag(file)
        	 val outfile = new PrintWriter(new FileWriter(
        		new File(outDir, file.getName())), true)
        	 outfile.println(keywords.mkString(" "))
        	 outfile.flush()
        	 outfile.close()
         })
}

class BagOfKeywords(val dictFile: File) {
	
	val dictNER = new DictNER(Map("keyword" -> dictFile))
	
	def tag(f: File): List[String] = {
		val source = Source.fromFile(f)
		val content = (try source.mkString finally source.close)
		val cleanContent = content.replaceAll("\n", " ")
		                          .replaceAll("\\p{Punct}", " 0 ")
		                          .replaceAll("\\s+", " ")
		                          .toLowerCase()
		val chunks = dictNER.chunk(cleanContent)
		chunks.map(chunk => cleanContent.slice(chunk.start, chunk.end)
				                        .replaceAll(" ", "_"))
	}
}