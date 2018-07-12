import sbt._


object Credentials {
  val version = "0.1.0-SNAPSHOT"
  val name = "zeppelin-remote-run"
  val organization = "Jetbrains"
}


object Versions {
  val pluginVersion = "0.1.0-SNAPSHOT"
  val scalaVersion: String = "2.12.6"
  val ideaVersion = "181.5281.24"

  val scalaTestVersion = "3.0.5"
  val scalaJHttpVersion = "2.3.0"
  val sprayJsonVersion = "1.3.3"
  val jettyWebSocketVersion = "9.4.8.v20171121"
}


object Dependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTestVersion
  val scalaJHttp = "org.scalaj" %% "scalaj-http" % Versions.scalaJHttpVersion
  val sprayJson = "io.spray" %% "spray-json" % Versions.sprayJsonVersion
  val webSocket = "org.eclipse.jetty.websocket" % "websocket-client" % Versions.jettyWebSocketVersion

  val zeppelinDependencies: Seq[ModuleID] = Seq(
    scalaTest,
    scalaJHttp,
    sprayJson,
    webSocket)
}