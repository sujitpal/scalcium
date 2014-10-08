package com.mycompany.scalcium.langmodel

import java.io.File

import scala.collection.JavaConversions._
import scala.io.Source
import scala.util.Random

import org.encog.Encog
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.engine.network.activation.ActivationSoftMax
import org.encog.mathutil.randomize.RangeRandomizer
import org.encog.ml.data.MLDataSet
import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.back.Backpropagation

class EncogNNEval {
  
  val Debug = false
  val encoder = new OneHotEncoder(10)

  def evaluate(trainfile: File, decay: Float, hiddenLayerSize: Int, 
      numIters: Int, learningRate: Float, momentum: Float, 
      miniBatchSize: Int, earlyStopping: Boolean): 
      (Double, Double, BasicNetwork) = {
    // parse training file into a 50/50 training and validation set
    val datasets = parseFile(trainfile, 0.5F)
    val trainset = datasets._1; val valset = datasets._2
    // build network
    val network = new BasicNetwork()
    network.addLayer(new BasicLayer(null, true, 8 * 8))
    network.addLayer(new BasicLayer(new ActivationSigmoid(), true, hiddenLayerSize))
    network.addLayer(new BasicLayer(new ActivationSoftMax(), false, 10))
    network.getStructure().finalizeStructure()
    new RangeRandomizer(-1, 1).randomize(network)
    // set up trainer
    val trainer = new Backpropagation(network, trainset, learningRate, momentum)
    trainer.setBatchSize(miniBatchSize)
    var currIter = 0
    var trainError = 0.0D
    var valError = 0.0D
    var pValError = 0.0D
    var contLoop = false
    do {
      trainer.iteration()
      if (decay > 0.0F) trainer.setLearningRate(
        (1.0 - (decay * currIter / numIters) * learningRate))
      // calculate training and validation error
      trainError = error(network, trainset)
      valError = error(network, valset)
      if (Debug) {
        Console.println("Epoch: %d, Train error: %.3f, Validation Error: %.3f"
          .format(currIter, trainError, valError))
      }
      currIter += 1
      contLoop = shouldContinue(currIter, numIters, earlyStopping, 
        valError, pValError)
      pValError = valError
    } while (contLoop)
    trainer.finishTraining()
    Encog.getInstance().shutdown()
    (trainError, valError, network)
  }

  def parseFile(f: File, holdout: Float): (MLDataSet, MLDataSet) = {
    val trainset = new BasicMLDataSet()
    val valset = new BasicMLDataSet()
    Source.fromFile(f).getLines()
      .foreach(line => {
        val cols = line.split(",")
        val inputs = cols.slice(0, 64).map(_.toDouble / 64.0D)
        val output = encoder.encode(cols(64).toInt)
        if (Random.nextDouble < holdout)
          valset.add(new BasicMLData(inputs), new BasicMLData(output))
        else trainset.add(new BasicMLData(inputs), new BasicMLData(output))
      })
    (trainset, valset)
  } 
  
  def error(network: BasicNetwork, dataset: MLDataSet): Double = {
    var numCorrect = 0.0D
    var numTested = 0.0D
    val x = dataset.map(pair => {
      val predicted = network.compute(pair.getInput()).getData()
      val actual = encoder.decode(pair.getIdeal().getData())
      if (actual == predicted.indexOf(predicted.max)) numCorrect += 1.0D
      numTested += 1.0D
    })
    numCorrect / numTested
  }

  def shouldContinue(currIter: Int, numIters: Int, earlyStopping: Boolean,
      validationError: Double, prevValidationError: Double): Boolean = 
    if (earlyStopping) 
      (currIter < numIters && prevValidationError < validationError)
    else currIter < numIters  
}