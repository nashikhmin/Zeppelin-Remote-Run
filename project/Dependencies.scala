import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  val scalaJHttp =  "org.scalaj" %% "scalaj-http" % "2.3.0"
  val sprayJson =  "io.spray" %% "spray-json" % "1.3.3"
  val webSocket = "org.eclipse.jetty.websocket" % "websocket-client" % "9.4.8.v20171121"

  val mainDependencies: Seq[ModuleID] =  Seq(
    scalaTest,
    scalaJHttp,
    sprayJson,
    webSocket)
}
