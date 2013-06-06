package com.github.scalcium

class Tree(val root: String, val children: List[Tree]) {

  def allParagraphs(): List[String] = getNodesAtLevel(1)
  def allSentences(): List[String] = getNodesAtLevel(2)
  def allPhrases(): List[String] = getNodesAtLevel(3)
  def allWords(): List[String] = getNodesAtLevel(4)
  
  def getNodesAtLevel(level: Int): List[String] = {
    null  
  }
  
  override def toString(): String = {
    var buf = new StringBuilder()
    toString_r(buf, 0, this)
    buf.toString()
  }
  
  def toString_r(buf: StringBuilder, lvl: Int, t: Tree): Unit = {
    buf.append("." * lvl).append(t.root).append("\n")
    if (t.children != null)
      t.children.map(child => toString_r(buf, lvl + 1, child) + ",\n")      
  }
}