package com.mycompany.scalcium.pipeline

import com.github.jenshaase.uimascala.core.description._
import org.apache.uima.jcas.tcas.Annotation
import org.apache.uima.cas.Feature

@TypeSystemDescription
object TypeSystem {

  val Entity = Annotation {
    val entityType = Feature[String]
  }
}