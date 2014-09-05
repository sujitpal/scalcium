import AssemblyKeys._

assemblySettings

name := "scalcium"

version := "0.1"

scalaVersion := "2.10.2"

unmanagedClasspath in Runtime <+= (baseDirectory) map {
  bd => Attributed.blank(bd / "src/main/resources")
}

resolvers ++= Seq(
  "Neo4j-Contrib" at "http://m2.neo4j.org/content/groups/everything",
  "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "org.scalaz.stream" %% "scalaz-stream" % "0.4.1",
  "org.apache.opennlp" % "opennlp-maxent" % "3.0.3",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.4.1",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.4.1" classifier "models",
  "org.apache.lucene" % "lucene-core" % "4.6.0",
  "org.apache.lucene" % "lucene-queries" % "4.6.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.6.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.6.0",
  "org.apache.solr" % "solr-solrj" % "4.6.0",
  "org.neo4j" % "neo4j" % "1.9.6",
  "org.neo4j" % "neo4j-rest-graphdb" % "1.9",
  "com.github.jenshaase.uimascala" %% "uimascala-core" % "0.5.0-SNAPSHOT",
  "nz.ac.waikato.cms.weka" % "weka-dev" % "3.7.10",
  "mx.bigdata.jcalais" % "j-calais" % "1.0",
  "com.likethecolor" % "alchemy" % "1.1.2",
  "org.deeplearning4j" % "deeplearning4j-core" % "0.0.3.2-SNAPSHOT",
//  "org.deeplearning4j" % "deeplearning4j-scaleout-akka" % "0.0.3.2-SNAPSHOT",
//  "org.deeplearning4j" % "deeplearning4j-scaleout-akka-word2vec" % "0.0.3.2-SNAPSHOT",
  "org.apache.commons" % "commons-lang3" % "3.0",
  "net.sourceforge.collections" % "collections-generic" % "4.01",
  "commons-beanutils" % "commons-beanutils" % "1.8.3",
  "commons-io" % "commons-io" % "2.4",
  "io.spray" %% "spray-json" % "1.2.6",
  "log4j" % "log4j" % "1.2.14",
  "com.novocode" % "junit-interface" % "0.8" % "test"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

