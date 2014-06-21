import sbt._
import sbt.Keys._

object ScalciumBuild extends Build {

  lazy val scalcium = Project(
    id = "scalcium",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "scalcium",
      organization := "com.healthline",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.11.1"
      // add other settings here
    )
  )
}
