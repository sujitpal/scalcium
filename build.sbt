import AssemblyKeys._

assemblySettings

name := "scalcium"

version := "0.1"

scalaVersion := "2.10.2"

unmanagedClasspath in Runtime <+= (baseDirectory) map {
  bd => Attributed.blank(bd / "src/main/resources")
}

resolvers ++= Seq(
  "spray" at "https://repo.spray.io/",
  "Neo4j-Contrib" at "http://m2.neo4j.org/content/groups/everything"
)


libraryDependencies ++= Seq(
  "org.apache.opennlp" % "opennlp-maxent" % "3.0.3",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
  "org.apache.lucene" % "lucene-core" % "4.6.0",
  "org.apache.lucene" % "lucene-queries" % "4.6.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.6.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.6.0",
  "org.apache.solr" % "solr-solrj" % "4.6.0",
  "org.neo4j" % "neo4j" % "1.9.6",
  "org.neo4j" % "neo4j-rest-graphdb" % "1.9",
  "org.apache.commons" % "commons-lang3" % "3.0",
  "net.sourceforge.collections" % "collections-generic" % "4.01",
  "commons-beanutils" % "commons-beanutils" % "1.8.3",
  "commons-io" % "commons-io" % "2.4",
  "io.spray" %%  "spray-json" % "1.2.5",
  "log4j" % "log4j" % "1.2.14",
  "com.novocode" % "junit-interface" % "0.8" % "test"
)
