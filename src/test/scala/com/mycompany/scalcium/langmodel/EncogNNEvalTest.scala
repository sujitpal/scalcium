package com.mycompany.scalcium.langmodel

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

import org.junit.Test

class EncogNNEvalTest {

  val trainfile = new File("src/main/resources/langmodel/optdigits_train.txt")
  val testfile = new File("src/main/resources/langmodel/optdigits_test.txt")
  
  @Test
  def testVaryLearningRateAndMomentum(): Unit = {
    val results = new PrintWriter(new FileWriter(
      new File("results1.csv")), true)
    val nneval = new EncogNNEval()
    val weightDecay = 0.0F
    val numHiddenUnit = 10
    val numIterations = 70
    val learningRates = Array[Float](0.002F, 0.01F, 0.05F, 0.2F, 1.0F, 5.0F, 20.0F)
    val momentums = Array[Float](0.0F, 0.9F)
    val miniBatchSize = 10
    val earlyStopping = false
    var lineNo = 0
    for (learningRate <- learningRates;
         momentum <- momentums) {
      runAndReport(nneval, results, trainfile, weightDecay, numHiddenUnit, 
        numIterations, learningRate, momentum, miniBatchSize, earlyStopping,
        lineNo == 0)
      lineNo += 1
    }
    results.flush()
    results.close()
  }
  
  @Test
  def testVaryLearningRateAndMomentumWithEarlyStopping(): Unit = {
    val results = new PrintWriter(new FileWriter(
      new File("results2.csv")), true)
    val nneval = new EncogNNEval()
    val weightDecay = 0.0F
    val numHiddenUnit = 10
    val numIterations = 70
    val learningRates = Array[Float](0.002F, 0.01F, 0.05F, 0.2F, 1.0F, 5.0F, 20.0F)
    val momentums = Array[Float](0.0F, 0.9F)
    val miniBatchSize = 10
    val earlyStopping = true
    var lineNo = 0
    for (learningRate <- learningRates;
         momentum <- momentums) {
      runAndReport(nneval, results, trainfile, weightDecay, numHiddenUnit, 
        numIterations, learningRate, momentum, miniBatchSize, earlyStopping,
        lineNo == 0)
      lineNo += 1
    }
    results.flush()
    results.close()
  }
  
  @Test
  def testVaryWeightDecay(): Unit = {
    val results = new PrintWriter(new FileWriter(
      new File("results3.csv")), true)
    val nneval = new EncogNNEval()
    val weightDecays = Array[Float](10.0F, 1.0F, 0.0F, 0.1F, 0.01F, 0.001F)
    val numHiddenUnit = 10
    val numIterations = 70
    val learningRate = 0.05F
    val momentum = 0.0F
    val miniBatchSize = 10
    val earlyStopping = true
    var lineNo = 0
    for (weightDecay <- weightDecays) {
      runAndReport(nneval, results, trainfile, weightDecay, numHiddenUnit, 
        numIterations, learningRate, momentum, miniBatchSize, earlyStopping,
        lineNo == 0)
      lineNo += 1
    }
    results.flush()
    results.close()
  }
  
  @Test
  def testVaryHiddenUnits(): Unit = {
    val results = new PrintWriter(new FileWriter(
      new File("results4.csv")), true)
    val nneval = new EncogNNEval()
    val weightDecay = 0.0F
    val numHiddenUnits = Array[Int](10, 50, 100, 150, 200, 250, 500)
    val numIterations = 70
    val learningRate = 0.05F
    val momentum = 0.0F
    val miniBatchSize = 10
    val earlyStopping = true
    var lineNo = 0
    for (numHiddenUnit <- numHiddenUnits) {
      runAndReport(nneval, results, trainfile, weightDecay, numHiddenUnit, 
        numIterations, learningRate, momentum, miniBatchSize, earlyStopping,
        lineNo == 0)
      lineNo += 1
    }
    results.flush()
    results.close()
  }
  
  @Test
  def testFinalRun(): Unit = {
    val nneval = new EncogNNEval()
    val weightDecay = 0.0F
    val numHiddenUnit = 200
    val numIterations = 1000
    val learningRate = 0.1F
    val momentum = 0.9F
    val miniBatchSize = 100
    val earlyStopping = true
    val scores = nneval.evaluate(trainfile, weightDecay, numHiddenUnit, 
      numIterations, learningRate, momentum, miniBatchSize, earlyStopping) 
    // verify on test set
    val testds = nneval.parseFile(testfile, 0.0F)
    val network = scores._3
    val testError = nneval.error(network, testds._1)
    Console.println("Train Error: %.3f, Validation Error: %.3f, Test Error: %.3f"
      .format(scores._1, scores._2, testError))
  }
  
  def runAndReport(nneval: EncogNNEval, results: PrintWriter, 
      trainfile: File, weightDecay: Float, numHiddenUnit: Int, 
      numIterations: Int, learningRate: Float, momentum: Float, 
      miniBatchSize: Int, earlyStopping: Boolean,
      writeHeader: Boolean): Unit = {
    val scores = nneval.evaluate(trainfile, weightDecay, numHiddenUnit, 
      numIterations, learningRate, momentum, miniBatchSize, earlyStopping) 
    if (writeHeader)
      results.println("DECAY\tHUNITS\tITERS\tLR\tMOM\tBS\tES\tTRNERR\tVALERR")
    results.println("%.3f\t%d\t%d\t%.3f\t%.3f\t%d\t%d\t%.3f\t%.3f"
      .format(weightDecay, numHiddenUnit, numIterations, learningRate, 
        momentum, miniBatchSize, if (earlyStopping) 1 else 0, 
        scores._1, scores._2))
  }
}