package com.mycompany.scalcium.coref

import opennlp.tools.coref.DefaultLinker
import opennlp.tools.coref.LinkerMode
import opennlp.tools.parser.Parse
import com.mycompany.scalcium.tokenizers.Tokenizer
import opennlp.tools.coref.mention.DefaultParse
import opennlp.tools.parser.ParserModel
import java.io.FileInputStream
import opennlp.tools.parser.ParserFactory
import opennlp.tools.cmdline.parser.ParserTool
import net.didion.jwnl.JWNL
import net.didion.jwnl.dictionary.Dictionary

class OpenNLPCorefResolver extends CorefResolver {

  val LinkerConfDir = "src/main/resources/opennlp/models/coref"
  val ParserModelFile = "src/main/resources/opennlp/models/en_parser_chunking.bin"
  val WordnetDictDir = "src/main/resources/wnconfig.xml"

  JWNL.initialize(new FileInputStream(WordnetDictDir))
  val dict = Dictionary.getInstance()
  val tokenizer = Tokenizer.getTokenizer("opennlp")
  val parserModel = new ParserModel(new FileInputStream(ParserModelFile))
  val parser = ParserFactory.create(parserModel)
  val linker = new DefaultLinker(LinkerConfDir, LinkerMode.TEST)
  linker.getMentionFinder().setCoordinatedNounPhraseCollection(true)
  linker.getMentionFinder().setPrenominalNamedEntityCollection(true)
  
  override def resolve(text: String): List[(CorefTriple,List[CorefTriple])] = {
    val mentions = tokenizer.sentTokenize(text)
      .zipWithIndex
      .map(sentIndex => {
        val parse = parseSentence(sentIndex._1)
        val parseWrapper = new DefaultParse(parse, sentIndex._2)
        linker.getMentionFinder().getMentions(parseWrapper)
      })
      .flatten
      .toArray
    val discourseEntities = linker.getEntities(mentions).toList
//      .map(mention => mention.getParse().getSentenceNumber() + "," + 
//          mention.getParse().getSpan())
    Console.println(discourseEntities)
    ???
  }
  
  def parseSentence(sentence: String): Parse = {
    ParserTool.parseLine(sentence, parser, 1).toList.head
  }
}