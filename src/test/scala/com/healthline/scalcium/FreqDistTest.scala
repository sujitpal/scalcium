package com.healthline.scalcium

import util.Random
import scala.math
import org.junit.Test
import org.junit.Assert

class FreqDistTest {

  val fd = new FreqDist()
  val xs = Seq.fill(1000)(math.abs(Random.nextInt % 10))
  xs.foreach(x => fd.inc(x))
  
  @Test def testGetItems(): Unit = {
    val items = fd.items()
    Assert.assertTrue(items.size > 0)
  }
  
  @Test def testN(): Unit = {
    val n = fd.N()
    val total = fd.keys().map(fd.count(_)).foldLeft(0)(_ + _)
    Assert.assertEquals(n, total)
  }
  
  @Test def testB(): Unit = {
    val bgt0 = fd.keys().filter(x => fd.count(x) > 0).size
    Assert.assertEquals(bgt0, fd.B())
  }
  
  @Test def testCountAndFreq(): Unit = {
    val counts = fd.keys().map(fd.count(_)).foldLeft(0)(_ + _)
    val freqs = fd.keys().map(fd.freq(_)).foldLeft(0.0F)(_ + _)
    Assert.assertEquals(counts, freqs * fd.N(), 0.0001F)
  }
}