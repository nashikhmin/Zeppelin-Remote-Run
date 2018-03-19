name := "zeppelin-remote"

version := "0.1"

scalaVersion := "2.12.4"

val akkaV = "10.1.0"

libraryDependencies ++= Seq(
  "org.eclipse.jetty.websocket" % "websocket-client" % "9.4.8.v20171121",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "io.spray" %% "spray-json" % "1.3.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test")