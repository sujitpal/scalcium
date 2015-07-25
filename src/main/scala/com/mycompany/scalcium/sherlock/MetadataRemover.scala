package com.mycompany.scalcium.sherlock

class MetadataRemover {
    
    def removeMetadata(text: String): String = {
        val lines = text.split("\n")
        val bounds = lines.zipWithIndex
                          .filter(li => 
                              li._1.startsWith("*** START OF THIS PROJECT") ||
                              li._1.startsWith("*** END OF THIS PROJECT"))
                          .map(li => li._2)
                          .toList
        lines.slice(bounds(0) + 1, bounds(1))
             .filter(line => looksLikeStoryLine(line))
             .mkString("\n")
    }
    
    def looksLikeStoryLine(line: String): Boolean = {
        (!line.startsWith("Produced by") && 
            !line.startsWith("By") &&
            !line.startsWith("by") &&
            !line.startsWith("End of the Project") &&
            !line.startsWith("End of Project") &&
            !line.contains("Arthur Conan Doyle") &&
            !(line.trim.size > 0 && line.toUpperCase().equals(line)))
    }

}
