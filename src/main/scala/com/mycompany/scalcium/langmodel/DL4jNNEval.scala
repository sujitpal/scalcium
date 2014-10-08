package com.mycompany.scalcium.langmodel

import java.io.File

import org.apache.commons.math3.random.MersenneTwister
import org.deeplearning4j.datasets.iterator.CSVDataSetIterator
import org.deeplearning4j.distributions.Distributions
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.models.classifiers.dbn.DBN
import org.deeplearning4j.nn.WeightInit
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.nd4j.linalg.api.activation.Activations
import org.nd4j.linalg.lossfunctions.LossFunctions

class DL4jNNEval {

  val NumTrainingInstances = 3823
  val evaluation = new Evaluation()
  
  def evaluate(trainfile: File, decay: Float, hiddenLayerSize: Int, 
      numIters: Int, learningRate: Float, momentum: Float, 
      miniBatchSize: Int): (Double, Double) = {
	// set up the DBN
    val rng = new MersenneTwister(123)
    val conf = new NeuralNetConfiguration.Builder()
      .learningRate(learningRate)
      .momentum(momentum)
      .iterations(numIters)
      .regularization(decay > 0.0F)
      .regularizationCoefficient(decay)
      .lossFunction(LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY)
      .rng(rng)
      .dist(Distributions.uniform(rng))
      .activationFunction(Activations.tanh())
      .weightInit(WeightInit.DISTRIBUTION)
      .nIn(64)  // 8*8 pixels per handwritten digit
      .nOut(10) // softmax 0-9
      .build()
    val dbn = new DBN.Builder()
      .configure(conf)
      .hiddenLayerSizes(Array[Int](hiddenLayerSize))
      .forceEpochs()
      .build()
	dbn.getOutputLayer().conf().setActivationFunction(Activations.softMaxRows())
	dbn.getOutputLayer().conf().setLossFunction(LossFunctions.LossFunction.MCXENT)
    // read input data
    val it = new CSVDataSetIterator(
      miniBatchSize * 2, NumTrainingInstances, trainfile, 64)
    val dataset = it.next(miniBatchSize * 2)
    dataset.normalizeZeroMeanZeroUnitVariance()
    val split = dataset.splitTestAndTrain(miniBatchSize)
    val trainset = split.getTrain()
    val testset = split.getTest()
    dbn.fit(trainset)
    evaluation.eval(dbn.output(trainset.getFeatureMatrix()), trainset.getLabels())
    val trainf1 = evaluation.f1()
    evaluation.eval(dbn.output(testset.getFeatureMatrix()), testset.getLabels())
    val testf1 = evaluation.f1()
    (trainf1, testf1)
  }
}
