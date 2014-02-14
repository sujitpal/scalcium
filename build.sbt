import AssemblyKeys._

assemblySettings

name := "scalcium"

version := "0.1"

scalaVersion := "2.10.2"

unmanagedClasspath in Runtime <+= (baseDirectory) map {
  bd => Attributed.blank(bd / "src/main/resources")
}

libraryDependencies ++= Seq(
  "org.apache.opennlp" % "opennlp-maxent" % "3.0.3",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
  "org.apache.lucene" % "lucene-core" % "4.6.0",
  "org.apache.lucene" % "lucene-queries" % "4.6.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.6.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.6.0",
  "org.apache.solr" % "solr-solrj" % "4.6.0",
  "org.apache.commons" % "commons-lang3" % "3.0",
  "net.sourceforge.collections" % "collections-generic" % "4.01",
  "commons-beanutils" % "commons-beanutils" % "1.8.3",
  "commons-io" % "commons-io" % "2.4",
  "log4j" % "log4j" % "1.2.14",
  "com.novocode" % "junit-interface" % "0.8" % "test"
)
