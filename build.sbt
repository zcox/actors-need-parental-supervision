organization := "iascala"

name := "actors-need-parental-supervision"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % Version.akka,
  "com.typesafe.akka" % "akka-slf4j" % Version.akka,
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime",
  "com.typesafe.akka" % "akka-testkit" % Version.akka % "test",
  "org.specs2" %% "specs2" % "1.12.4.1" % "test"
)
