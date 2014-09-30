package com.mycompany.scalcium.langmodel

import java.io.File

import scala.Array._
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Random

import org.encog.Encog
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.engine.network.activation.ActivationTANH
import org.encog.mathutil.matrices.Matrix
import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.back.Backpropagation
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation

class LangModelNetwork(infile: File) {

  val sentences = Source.fromFile(infile).getLines.toList
  val vocab = buildVocab(sentences)
  val encoder = new OneHotEncoder(vocab.size)
  
  // embed each word into a dense 50D representation
  val embeddings = computeWordEmbeddings(vocab, 50, false)

  val quadgrams = buildQuadGrams(sentences, vocab)
  val trainDataset = new BasicMLDataSet()
  val testDataset = new BasicMLDataSet()
  quadgrams.map(quadgram => {
    val (ingrams, outgram) = quadgram.splitAt(3)
    val inputs = ingrams.flatMap(ingram => embeddings(vocab(ingram))).toArray
    val output = encoder.encode(vocab(outgram.head))
    // split train/test 75/25
    if (Random.nextDouble < 0.75D)
      trainDataset.add(new BasicMLData(inputs), new BasicMLData(output))
    else testDataset.add(new BasicMLData(inputs), new BasicMLData(output))
  })
  val network = new BasicNetwork()
  network.addLayer(new BasicLayer(null, true, 50 * 3)) // 50 neurons per word
  network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 200))
  network.addLayer(new BasicLayer(new ActivationSigmoid(), false, vocab.size))
  network.getStructure().finalizeStructure()
  network.reset()
  val trainer = new Backpropagation(network, trainDataset, 0.1, 0.9)
  trainer.setBatchSize(100)
  trainer.setThreadCount(Runtime.getRuntime.availableProcessors())
  var epoch = 0
  do {
    trainer.iteration()
    Console.println("Epoch: %d, error: %5.3f".format(epoch, trainer.getError()))
    epoch += 1
  } while (trainer.getError() > 0.01)
  trainer.finishTraining()
  // measure performance on test set
  var numOk = 0.0D
  var numTests = 0.0D
  testDataset.map(pair => {
    val predicted = network.compute(pair.getInput()).getData()
    val actual = encoder.decode(pair.getIdeal().getData())
    if (actual == predicted.indexOf(predicted.max)) numOk += 1.0D
    numTests += 1.0D
  })
  Console.println("Test set error: %5.3f".format(numOk / numTests))
  Encog.getInstance().shutdown()
  
  def computeWordEmbeddings(vocab: Map[String,Int], targetSize: Int,
      trace: Boolean): Map[Int,Array[Double]] = {
    val inputs = (0 until vocab.size)
      .map(i => encoder.encode(i))
      .toArray
    val dataset = new BasicMLDataSet(inputs, inputs) 
    val network = new BasicNetwork()
    network.addLayer(new BasicLayer(null, true, vocab.size))
    network.addLayer(new BasicLayer(new ActivationTANH(), false, targetSize))
    network.getStructure().finalizeStructure()
    network.reset()
    val trainer = new ResilientPropagation(network, dataset)
    var epoch = 0
    do {
      trainer.iteration()
      if (trace) Console.println("Epoch: %d, error: %5.3f"
        .format(epoch, trainer.getError()))
      epoch += 1
    } while (trainer.getError() > 0.001)
    trainer.finishTraining()
    val embeddings = dataset.zipWithIndex.map(pz => {
        val predicted = network.compute(pz._1.getInput())
        (pz._2, predicted.getData())
      })
      .toMap
    Encog.getInstance().shutdown()
    embeddings
  }
  
  def getWords(sentence: String): List[String] = 
    sentence.toLowerCase.split("\\s+")
      .filter(word => ! word.matches("\\p{Punct}")) // remove puncts
      .filter(word => word.trim().length > 0)       // remove empty words
      .toList
    
  def buildVocab(sentences: List[String]): Map[String,Int] =
    sentences.flatMap(sentence => getWords(sentence))
      .toSet        // remove duplicates
      .zipWithIndex // word => word_id
      .toMap

  def buildQuadGrams(sentences: List[String], vocab: Map[String,Int]): 
	  List[List[String]] = 
    sentences.map(sentence => getWords(sentence))
      .map(words => words.sliding(4))
      .flatten
      .filter(quad => quad.size == 4)

  def printSparse(matrix: Matrix, numRows: Int): String = {
    val buffer = ArrayBuffer[(Int,Int,Double)]()
    (0 until numRows).foreach(rownum => {
      (0 until matrix.getCols()).foreach(colnum => {
        if (matrix.get(rownum, colnum) > 0.0D) buffer += ((rownum, colnum, 1.0D))
      })
    })
    buffer.mkString(",")
  }
}

class OneHotEncoder(val arraySize: Int) {
  
  def encode(idx: Int): Array[Double] = {
    val encoded = Array.fill[Double](arraySize)(0.0D)
    encoded(idx) = 1.0
    encoded
  }
  
  def decode(activations: Array[Double]): Int = {
    val idxs = activations.zipWithIndex
      .filter(a => a._1 > 0.0D)
    if (idxs.size == 1) idxs.head._2 else -1
  }
}