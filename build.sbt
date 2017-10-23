name := "mapdrawer-scala"

version := "0.1"

scalaVersion := "2.12.4"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",
  "com.github.pureconfig" %% "pureconfig" % "0.7.2",
  "org.scodec" %% "scodec-core" % "1.10.3",
  "org.scodec" %% "scodec-akka" % "0.3.0",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
)

mainClass in assembly := Some("Main")