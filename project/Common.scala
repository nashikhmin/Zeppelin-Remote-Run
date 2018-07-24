import sbt.Keys._
import sbt._

import scala.language.{implicitConversions, postfixOps}

object Common {
  def newProject(projectName: String, base: File): Project = {
    Project(projectName, base).settings(
      name := projectName,
      organization := Credentials.organization,
      scalaVersion := Versions.scalaVersion,
      unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "scala",
      unmanagedSourceDirectories in Test += baseDirectory.value / "test" / "scala",
      unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
      unmanagedResourceDirectories in Test += baseDirectory.value / "testResources",
      updateOptions := updateOptions.value.withCachedResolution(true)
    )
  }
}