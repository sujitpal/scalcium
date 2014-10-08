package com.mycompany.scalcium.langmodel

import org.junit.Test
import java.io.File

class DL4jNNEvalTest {
  
  val datadir = new File("src/main/resources/langmodel")
  val trainfile = new File(datadir, "optdigits_train.txt")

  @Test
  def testRunAllTests(): Unit = {
    
    // parameters
    val NumIterations = 70
    val MiniBatchSize = 10
    
    val nneval = new DL4jNNEval()
    val learningRates = Array[Float](0.002F, 0.01F, 0.05F, 0.2F, 1.0F, 5.0F, 20.0F)
    val momentums = Array[Float](0.0F, 0.9F)
    momentums.foreach(momentum => {
      learningRates.foreach(learningRate => {
        val scores = nneval.evaluate(
          trainfile, 0.0F, 10, NumIterations, learningRate, momentum, MiniBatchSize)
        Console.println(">>> %.3f\t%.3f\t%.3f\t%.3f".format(momentum, learningRate, 
          scores._1, scores._2))
      })
    })
    // best result: momentum 0.0 and learning rate 0.01
    val weightDecays = Array[Float](0.00001F, 0.001F, 0.01F, 0.1F, 0.0F, 1.0F, 10.0F)
    weightDecays.foreach(weightDecay => {
      val scores = nneval.evaluate(
        trainfile, weightDecay, 10, NumIterations, 0.01F, 0.0F, MiniBatchSize)
      Console.println(">>> %.3f\t%.3f\t%.3f".format(weightDecay, 
        scores._1, scores._2))
    })
    // best result with 0.01 weight decay
    val numHiddenUnits = Array[Int](10, 30, 100, 130, 170)
    numHiddenUnits.foreach(numHiddenUnit => {
      val scores = nneval.evaluate(
       trainfile, 0.01F, numHiddenUnit, NumIterations, 0.01F,  0.0F, MiniBatchSize)
      Console.println(">>> %d\t%.3f\t%.3f".format(numHiddenUnit, scores._1, scores._2))
    })
  }
}