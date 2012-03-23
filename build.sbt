name := "csx-downloader"

version := "0.1-SNAPSHOT"

organization := "cite-net"

scalaVersion := "2.9.1"

resolvers ++= Seq("Sonatype Nexus releases" at "https://oss.sonatype.org/content/repositories/releases", 
	"Sonatype Nexus snapshots" at "https://oss.sonatype.org/content/repositories/snapshots", 
	//"Scala-Tools repo" at "http://scala-tools.org/repo-releases/",
	"Central repository" at "http://repo1.maven.org/maven2",
	"T repo" at "http://maven.twttr.com/")

// General dependencies (testing, logging, etc.)
libraryDependencies ++= Seq(
	"org.slf4j" % "slf4j-nop" % "1.6.0" % "runtime",
	//"org.streum" %% "configrity-core" % "0.10.0",
	"thrift" % "libthrift" % "0.5.0" from "http://maven.twttr.com/thrift/libthrift/0.5.0/libthrift-0.5.0.jar",
	"com.twitter" % "util" % "1.10.1",
	"org.scalatest" %% "scalatest" % "1.6.1" % "test",
	"com.novocode" % "junit-interface" % "0.7" % "test->default")