package com.mycompany.scalcium.langmodel

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Random

import org.encog.mathutil.rbf.RBFEnum
import org.encog.ml.data.basic.BasicMLData
import org.encog.ml.data.basic.BasicMLDataSet
import org.encog.neural.som.SOM
import org.encog.neural.som.training.basic.BasicTrainSOM
import org.encog.neural.som.training.basic.neighborhood.NeighborhoodRBF

class WordClusterSOM(infile: File, outfile: File) {

  // read input data and build dataset
  val words = ArrayBuffer[String]()
  val dataset = new BasicMLDataSet()
  Source.fromFile(infile).getLines().foreach(line => {
      val cols = line.split(",")
      val word = cols(cols.length - 1)
      val vec = cols.slice(0, cols.length - 1)
        .map(e => e.toDouble)
      dataset.add(new BasicMLData(vec))
      words += word
  })
  
  // gensim's word2vec gives us word vectors of size 100 (100 input neurons), 
  // we want to cluster it onto a 50x50 grid (2500 output neurons).
  val som = new SOM(100, 50 * 50)
  som.reset()
  val neighborhood = new NeighborhoodRBF(RBFEnum.Gaussian, 50, 50)
  val learningRate = 0.01
  val train = new BasicTrainSOM(som, learningRate, dataset, neighborhood)
  train.setForceWinner(false)
  train.setAutoDecay(1000, 0.8, 0.003, 30, 5) // 1000 epochs, learning rate
                                              // decreased from 0.8-0.003,
                                              // radius decreased from 30-5
  // train network - online training
  (0 until 1000).foreach(i => {
    // randomly select single word vector to train with at each epoch
    val idx = (Random.nextDouble * words.size).toInt
    val data = dataset.get(idx).getInput()
    train.trainPattern(data)
    train.autoDecay()
    Console.println("Epoch %d, Rate: %.3f, Radius: %.3f, Error: %.3f"
      .format(i, train.getLearningRate(), train.getNeighborhood().getRadius(), 
        train.getError()))
  })
  
//  // train network - batch training (takes long time but better results)
//  (0 until 1000).foreach(i => {
//    train.iteration()
//    train.autoDecay()
//    Console.println("Epoch %d, Rate: %.3f, Radius: %.3f, Error: %.3f"
//      .format(i, train.getLearningRate(), train.getNeighborhood().getRadius(), 
//        train.getError()))
//  })
  
  // prediction time
  val writer = new PrintWriter(new FileWriter(outfile), true)
  dataset.getData().zip(words)
    .foreach(dw => {
      val xy = convertToXY(som.classify(dw._1.getInput())) // find BMU id/coords
      writer.println("%s\t%d\t%d".format(dw._2, xy._1, xy._2))
  })
  writer.flush()
  writer.close()

  def convertToXY(pos: Int): (Int, Int) = {
    val x = Math.floor(pos / 50).toInt
    val y = pos - (50 * x)
    (x, y)
  }
}
