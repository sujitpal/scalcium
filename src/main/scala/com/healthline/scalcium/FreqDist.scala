package com.healthline.scalcium

class FreqDist {

  val fd = scala.collection.mutable.Map[Any,Int]()
  var n = 0
  
  /**
   * Add sample to distribution.
   */
  def inc(sample: Any): Unit = inc(sample, 1)
  
  /**
   * Add count samples to distribution.
   */
  def inc(sample: Any, count: Int): Unit = {
    val currentCount = fd.getOrElse(sample, 0)
    fd(sample) = currentCount + count
    n += count
  }
  
  /**
   * Return total number of sample outcomes.
   */
  def N(): Int = n

  /**
   * Returns total number of samples with positive counts.
   */
  def B(): Int = fd.filter(x => x._2 > 0).size
  
  /**
   * Returns a list of all samples in this distribution.
   */
  def samples(): List[Any] = fd.keys.toList
  
  /**
   * Returns a list of samples that occur exactly r times.
   */
  def Nr(r: Int): Int = fd.filter(x => x._2 == r).size

  /**
   * Return count of specified sample.
   */
  def count(sample: Any): Int = fd(sample)
  
  /**
   * Return frequency of specific sample (count divided by N)
   */
  def freq(sample: Any): Float = (count(sample).asInstanceOf[Float] / N())
  
  /**
   * Return list of (sample, count) tuples ordered by count desc.
   */
  def items(): List[(Any,Int)] = fd.toList.sortWith(_._2 > _._2)
  
  /**
   * Return list of samples ordered by count desc.
   */
  def keys(): List[Any] = items().map(_._1)
  
  /**
   * Clears the underlying data structure.
   */
  def clear(): Unit = {
    fd.clear()
    n = 0
  }
}