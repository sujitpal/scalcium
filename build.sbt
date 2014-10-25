import AssemblyKeys._

assemblySettings

name := "scalcium"

version := "0.1"

scalaVersion := "2.10.4"

unmanagedClasspath in Runtime <+= (baseDirectory) map {
  bd => Attributed.blank(bd / "src/main/resources")
}

resolvers ++= Seq(
  // TODO: for dl4j until 0.0.3.2 is up on maven central
  "Local Maven Repo" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Bliki Repo" at "http://gwtwiki.googlecode.com/svn/maven-repository/",
  "Neo4j-Contrib" at "https://raw.github.com/neo4j-contrib/m2/master/releases",
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
  //"com.github.jenshaase.uimascala" %% "uimascala-core" % "0.5.0-SNAPSHOT",
  //"org.apache.uima" % "uimafit-core" % "2.1.0",
  //"org.apache.uima" % "uimaj-tools" % "2.6.0",
  "nz.ac.waikato.cms.weka" % "weka-dev" % "3.7.10",
  "mx.bigdata.jcalais" % "j-calais" % "1.0",
  "com.likethecolor" % "alchemy" % "1.1.2",
  "org.apache.commons" % "commons-lang3" % "3.0",
  "org.apache.commons" % "commons-math3" % "3.0",
  "org.encog" % "encog-core" % "3.2.0",
  "org.deeplearning4j" % "deeplearning4j-core" % "0.0.3.2",
  "org.deeplearning4j" % "deeplearning4j-scaleout-akka" % "0.0.3.2",
  "org.deeplearning4j" % "deeplearning4j-nlp" % "0.0.3.2",
  "org.nd4j" % "nd4j-api" % "0.0.3.5-SNAPSHOT",
  "org.nd4j" % "nd4j-jblas" % "0.0.3.5-SNAPSHOT",
  "info.bliki.wiki" % "bliki-core" % "3.0.19",
  "net.htmlparser.jericho" % "jericho-html" % "3.3",
  "edu.washington.cs.knowitall" % "reverb-core" % "1.4.0",
  "org.apache.jena" % "apache-jena-libs" % "2.12.1",
  "commons-httpclient" % "commons-httpclient" % "3.1",
  "commons-beanutils" % "commons-beanutils" % "1.8.3",
  "commons-io" % "commons-io" % "2.4",
  "net.sourceforge.collections" % "collections-generic" % "4.01",
  "io.spray" %% "spray-json" % "1.2.6",
  "log4j" % "log4j" % "1.2.14",
  "com.novocode" % "junit-interface" % "0.8" % "test"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

