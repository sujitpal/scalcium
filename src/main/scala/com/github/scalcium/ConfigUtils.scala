package com.github.scalcium

import scala.io.Source

object ConfigUtils {

  val config = Source.fromFile("/prod/web/config/cm2.cf")
  val confMap: Map[String,String] = Map() ++ config.getLines().map(line => {
    if (line != null && line.trim().length() > 0 &&
        (! line.startsWith("#"))) {
      val kv = line.split("=")
      if (kv.length == 1) (kv(0), "")
      else (kv(0), kv(1))
    } else {
      // creates one extra record, never looked up
      ("_XXX_", "_XXX_")
    }
  })
  
  def getStringValue(content: String, key: String, defaultValue: String): String = 
    getValue[String](content, key, defaultValue)
  def getDefaultStringValue(key: String, defaultValue: String): String = 
    getDefaultValue[String](key, defaultValue)
  def getBooleanValue(content: String, key: String, defaultValue: Boolean): Boolean =
    getValue[Boolean](content, key, defaultValue).toString.toBoolean
  def getDefaultBooleanValue(key: String, defaultValue: Boolean): Boolean =
    getDefaultValue[Boolean](key, defaultValue).toString.toBoolean
  def getIntValue(content: String, key: String, defaultValue: Int): Int = 
    getValue[Int](content, key, defaultValue).toInt
  def getDefaultIntValue(key: String, defaultValue: Int): Int = 
    getDefaultValue[Int](key, defaultValue).toInt
  def getFloatValue(content: String, key: String, defaultValue: Float): Float = 
    getValue[Float](content, key, defaultValue).toFloat
  def getDefaultFloatValue(key: String, defaultValue: Float): Float =
    getDefaultValue[Float](key, defaultValue).toFloat
  
  ///////////////////////////////////////////////////////////////////
  
  def getValue[T](content: String, key: String, defaultValue: T): String = {
    if (content == null) getDefaultValue(key, defaultValue)
    else {
      val confKey = List(content, key).mkString(".")
      if (confMap.contains(confKey)) confMap(confKey)
      else getDefaultValue(key, defaultValue)
    }
  }
  
  def getDefaultValue[T](key: String, defaultValue: T): String = {
    val defKey = List("default", key).mkString(".")
    if (confMap.contains(defKey)) confMap(defKey)
    else defaultValue.toString
  }
}