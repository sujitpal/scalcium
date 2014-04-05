package com.healthline.scalcium.smoke

import org.junit.Test
import java.io.File
import org.apache.commons.io.FileUtils
import java.util.regex.Pattern

class SmokingClassifierTest {

  val datadir = new File("/home/sujit/Projects/med_data/smokes/data/")
  val trainfile = new File(datadir, "smokers_surrogate_train_all_version2.xml")
  val testfile = new File(datadir, "smokers_surrogate_test_all_groundtruth_version2.xml")
  val preprocessor = new Preprocessor()
  
  @Test
  def testBuildMultiClassArff(): Unit = {
    val ab = new ArffBuilder()
    ab.buildMulticlassArff(trainfile,
      new File(datadir, "smoke_mc.arff"))
  }

  @Test
  def testBuildSmokerNonSmokerArff(): Unit = {
    val ab = new ArffBuilder()
    ab.buildSmokerNonSmokerArff(trainfile,
      new File(datadir, "smoke_sns.arff"))
  }
  
  @Test
  def testBuildSubclassifierArffs(): Unit = {
    val ab = new ArffBuilder()
    ab.buildSubClassifierArrfs(trainfile, 
      new File(datadir, "smoke_subs.arff"), 
      new File(datadir, "smoke_subn.arff"))
  }

  @Test
  def testEvaluateMultiClass(): Unit = {
    val sc = new SmokingClassifier(
      new File(datadir, "smoke_mc_vectorized.arff"))
    val trainResult = sc.evaluate(trainfile, 
      preprocessor.multiClassTargets, 
      sc.trainedModel)
    Console.println("training accuracy=%f".format(trainResult))
    val testResult = sc.evaluate(testfile,
      preprocessor.multiClassTargets,
      sc.trainedModel)
    Console.println("test accuracy=%f".format(testResult))
  }
  
  @Test
  def testEvaluateSmokerNonSmoker(): Unit = {
    val sc = new SmokingClassifier(
      new File(datadir, "smoke_sns_vectorized.arff"))
    val trainResult = sc.evaluate(trainfile, 
      preprocessor.smokerNonSmokerTargets,
      sc.trainedModel)
    Console.println("training accuracy=%f".format(trainResult))
    val testResult = sc.evaluate(testfile, 
      preprocessor.smokerNonSmokerTargets,
      sc.trainedModel)
    Console.println("test accuracy=%f".format(testResult))
  }
  
  @Test
  def testEvaluateStackedClassifier(): Unit = {
    val sc = new SmokingClassifier(
      new File(datadir, "smoke_sns_vectorized.arff"),
      new File(datadir, "smoke_subs_vectorized.arff"),
      new File(datadir, "smoke_subn_vectorized.arff"))
    val trainResult = sc.evaluateStacked(trainfile, 
      sc.trainedModel, sc.trainedNonSmokSubModel, 
      sc.trainedSmokSubModel)
    Console.println("training accuracy=%f".format(trainResult))
    val testResult = sc.evaluateStacked(testfile, 
      sc.trainedModel, sc.trainedNonSmokSubModel, 
      sc.trainedSmokSubModel)
    Console.println("test accuracy=%f".format(testResult))
  }

}
