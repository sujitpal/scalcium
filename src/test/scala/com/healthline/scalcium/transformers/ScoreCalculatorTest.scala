package com.healthline.scalcium.transformers

import org.junit.Test
import scala.collection.mutable.ArrayBuffer
import com.healthline.util.Config
import org.junit.Assert
import org.junit.Test
import com.healthline.scalcium.utils.Tokenizer

class ScoreCalculatorTest {

  val text = "Fifty-two year old premenopausal Latino mother of five is here to discuss treatment options for her recently diagnosed breast cancer. She presented to her gynecologist with a complaint of right breast skin retraction at the 4 o'clock axis. Her axilla was clinically negative. Imaging study showed an ill-defined mass. Biopsy confirmed invasive ductal carcinoma. She elected to have a mastectomy with reconstruction and sentinel lymph node procedure for nodal staging. Final pathology revealed a high grade, ER/PR+, Her2 negative tumor. The maximum size of the tumor mass was measured at 1.0 cm with all surgical margins negative. Sentinel node biopsy revealed that two of two nodes were positive for metastasis. Due to metastasis in both lymph nodes, she elected to have a complete lymph node dissection ten days ago. Pathology reports that 1 of the thirteen nodes removed contained a small focus of metastatic ductal carcinoma (3+/15)."
  val tokenizer = new Tokenizer()
  
  @Test def testBaseScores(): Unit = {
    Config.setConfigDir("/prod/web/config")
    val phbuf = new ArrayBuffer[String]()
    tokenizer.sentTokenize(text).foreach(sentence => {
      phbuf ++= tokenizer.phraseTokenize(sentence)
    })
    val baseScores = ScoreCalculator.baseScores(phbuf.toList)
    baseScores.foreach(x => 
      Console.println(x._1.getCFN() + " (" + x._1.getImuid() + 
      ") => " + x._2))
    Assert.assertFalse(baseScores.isEmpty)
  }
}