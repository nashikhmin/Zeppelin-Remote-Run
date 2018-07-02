import Dependencies._

lazy val root = (project in file("."))
  .settings(
    organization := "org.jetbrains",
    name         := "zeppelin-remote-run",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.6",
      ideaBuild := "181.4203.550",
    libraryDependencies ++= mainDependencies,
    unmanagedJars in Compile += file(System.getProperty("java.home")).getParentFile / "lib" / "tools.jar"
  )
