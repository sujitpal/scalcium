import sbt._
import sbt.Keys._

object ScalciumBuild extends Build {

  lazy val scalcium = Project(
    id = "scalcium",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "scalcium",
      organization := "com.github",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2"
      // add other settings here
    )
  )
}
