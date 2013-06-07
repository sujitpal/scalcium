package com.healthline.scalcium

import org.junit.Test
import org.junit.Assert

class AbbreviationExpansionTest {

  val texts = Array[String](
    "The aim of the present study was to investigate the effect of some air pollutants and meteorological parameters on the survivability of airborne fungi. Fungi were collected by using a slit impactor sampler calibrated to draw 20 L/min, for 3 min. Nitrogen dioxide (NO(2)), sulfur dioxide (SO(2)), particulate matter (PM), relative humidity (RH %), temperature (T °C) and wind speed (WS) were also measured. Air samples were taken during the period from March 2006 to February 2007. Fungal concentrations ranged between 45 and 451 CFU/m(3) with an annual mean concentration of 216 CFU/m(3). The lowest fungal concentration was found in the summer, however the highest one was found in the autumn. NO(2,) SO(2) and PM averaged 83.66 μg/m(3), 67.01 μg/m(3), and 237.69 μg/m(3), respectively. T °C was positively and negatively correlated with Aspergillus (P = 0.000) and Penicillium (P = 0.007), respectively. RH% was positively correlated with total fungi (P = 0.001), Aspergillus (P = 0.002) and Cladosporium (P = 0.047). Multiple regression analysis showed that T °C and RH% were the most predicted variants. Non-significant correlations were found between fungal concentrations and air pollutants. Meteorological parameters were the critical factors affecting fungal survivability. Copyright © 2011 Elsevier B.V. All rights reserved.",
    "The nitrates (NO(3)-N) lost through subsurface drainage in the Midwest often exceed concentrations that cause deleterious effects on the receiving streams and lead to hypoxic conditions in the northern Gulf of Mexico. The use of drainage and water quality models along with observed data analysis may provide new insight into the water and nutrient balance in drained agricultural lands and enable evaluation of appropriate measures for reducing NO(3)-N losses.  DRAINMOD-NII, a carbon (C) and nitrogen (N) simulation model, was field tested for the high organic matter Drummer soil in Indiana and used to predict the effects of fertilizer application rate and drainage water management (DWM) on NO-N losses through subsurface drainage. The model was calibrated and validated for continuous corn (Zea mays L.) (CC) and corn-soybean [Glycine max (L.) Merr.] (CS) rotation treatments separately using 7 yr of drain flow and NO(3)-N concentration data. Among the treatments, the Nash-Sutcliffe efficiency of the monthly NO(3)-N loss predictions ranged from 0.30 to 0.86, and the percent error varied from -19 to 9%. The medians of the observed and predicted monthly NO(3)-N losses were not significantly different. When the fertilizer application rate was reduced ~20%, the predicted NO(3)-N losses in drain flow from the CC treatments was reduced 17% (95% confidence interval [CI], 11-25), while losses from the CS treatment were reduced by 10% (95% CI, 1-15). With DWM, the predicted average annual drain flow was reduced by about 56% (95% CI, 49-67), while the average annual NO(3)-N losses through drain flow were reduced by about 46% (95% CI, 32-57) for both tested crop rotations. However, the simulated NO(3)-N losses in surface runoff increased by about 3 to 4 kg ha(-1) with DWM. For the simulated conditions at the study site, implementing DWM along with reduced fertilizer application rates would be the best strategy to achieve the highest NO(3)-N loss reductions to surface water. The suggested best strategies would reduce the NO(3)-N losses to surface water by 38% (95% CI, 29-46) for the CC treatments and by 32% (95% CI, 23-40) for the CS treatments.",
    "International Business Machines (IBM) makes computers. IBM was recently in the news. In India Hindustan Computers Limited (HCL) is also popular. HCL makes minicomputers and provides Unix support."
  )
  val tokenizer = new Tokenizer()

  @Test def testAbbreviationExpansion(): Unit = {
    texts.foreach(text => {
      val otext = AbbreviationExpander.expand(text, tokenizer)
      Console.println("INPUT: " + text)
      Console.println("OUTPUT: " + otext)
    })
  }
  
  @Test def testCheckAbbreviationExpansion(): Unit = {
    val otext = AbbreviationExpander.expand(texts(2), tokenizer)
    val ibmPattern = "IBM".r
    val ibmMatchedIn = ibmPattern.findAllIn(texts(2)).toList.size
    val ibmMatchedOut = ibmPattern.findAllIn(otext).toList.size
    Assert.assertTrue(ibmMatchedIn > 0)
    Assert.assertEquals(0, ibmMatchedOut)
  }
}