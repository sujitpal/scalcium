package com.healthline.scalcium.smoke

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import scala.Array.canBuildFrom
import weka.classifiers.trees.J48
import weka.core.Attribute
import weka.core.Instances
import weka.core.SparseInstance
import weka.classifiers.Classifier
import weka.classifiers.functions.SMO
import weka.classifiers.rules.PART
import weka.core.Instance

case class TrainedModel(classifier: Classifier, 
    dataset: Instances, 
    vocab: Map[String,Int])
    
class SmokingClassifier(arffIn: File, 
    smokingArffIn: File = null, 
    nonSmokingArffIn: File = null) {

  val preprocessor = new Preprocessor()
  
  val trainedModel = trainJ48(arffIn)
  val trainedSmokSubModel = trainSMO(smokingArffIn)
  val trainedNonSmokSubModel = trainPART(nonSmokingArffIn)

  def evaluate(testfile: File, 
      targets: Map[String,Int], 
      model: TrainedModel): Double = {
    var numTested = 0D
    var numCorrect = 0D
    val rootElem = scala.xml.XML.loadFile(testfile)
    (rootElem \ "RECORD").map(record => {
      val smoking = (record \ "SMOKING" \ "@STATUS").text
      val text = (record \ "TEXT").text
        .split("\n")
        .filter(line => line.endsWith("."))
        .map(line => preprocessor.preprocess(line))
        .mkString(" ")
      val ypred = predict(text, model)
      numTested += 1D
      if (ypred == targets(smoking)) 
        numCorrect += 1D
    })
    100.0D * numCorrect / numTested
  }
  
  def evaluateStacked(testfile: File,
      topModel: TrainedModel,
      nonSmokingSubModel: TrainedModel,
      smokingSubModel: TrainedModel): Double = {
    var numTested = 0D
    var numCorrect = 0D
    val rootElem = scala.xml.XML.loadFile(testfile)
    (rootElem \ "RECORD").map(record => {
      val smoking = (record \ "SMOKING" \ "@STATUS").text
      val text = (record \ "TEXT").text
        .split("\n")
        .filter(line => line.endsWith("."))
        .map(line => preprocessor.preprocess(line))
        .mkString(" ")
      val topPred = predict(text, topModel)
      if (topPred == 1) { // smoking
        val subPred = predict(text, smokingSubModel)
        if (preprocessor.smokerSubTargets.contains(smoking) &&
            subPred == preprocessor.smokerSubTargets(smoking))
          numCorrect += 1
      } else { // non-smoking
        val subPred = predict(text, nonSmokingSubModel)
        if (preprocessor.nonSmokerSubTargets.contains(smoking) &&
            subPred == preprocessor.nonSmokerSubTargets(smoking))
          numCorrect += 1
      }
      numTested += 1
    })    
    100.0D * numCorrect / numTested
  }

  /////////////////// predict ////////////////////
  
  def predict(input: String, 
      model: TrainedModel): Int = {
    val inst = buildInstance(input, model)
    val pdist = model.classifier.distributionForInstance(
      inst)
    pdist.zipWithIndex.maxBy(_._1)._2
  }
  
  def buildInstance(input: String, 
      model: TrainedModel): Instance = {
    val inst = new SparseInstance(model.vocab.size)
    input.split(" ")
      .foreach(word => {
        if (model.vocab.contains(word)) {
          inst.setValue(model.vocab(word), 1)
        }
    })
    inst.setDataset(model.dataset)
    inst
  }

  /////////////////// train models //////////////////
  
  def trainJ48(arff: File): TrainedModel = {
    trainModel(arff, new J48())  
  }
  
  def trainSMO(arff: File): TrainedModel = {
    if (arff == null) null 
    else {
      val smo = new SMO()
      smo.setC(0.1D)
      smo.setToleranceParameter(0.1D)
      trainModel(arff, smo)
    }
  }
  
  def trainPART(arff: File): TrainedModel = {
    if (arff == null) null
    else trainModel(arff, new PART())
  }
  
  def trainModel(arff: File, 
      classifier: Classifier): TrainedModel = {
    val reader = new BufferedReader(new FileReader(arff))
    val _instances = new Instances(reader)
    reader.close()
    _instances.setClassIndex(0)
    val _vocab = scala.collection.mutable.Map[String,Int]()
    val e = _instances.enumerateAttributes()
    while (e.hasMoreElements()) {
      val attrib = e.nextElement().asInstanceOf[Attribute]
      if (! "class".equals(attrib.name())) {
        // replace the _binarized suffix
        val stripname = attrib.name().replace("_binarized", "")
        _vocab += ((stripname, attrib.index()))
      }
    }
    classifier.buildClassifier(_instances)
    TrainedModel(classifier, _instances, _vocab.toMap)
  }
}