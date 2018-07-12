import Common._
import org.jetbrains.sbtidea.Keys.{ideaExternalPlugins, updateIdea}


ideaDownloadDirectory in ThisBuild := homePrefix / ".RemoteRunPlugin" / "sdk"

ideaExternalPlugins += IdeaPlugin.Id("Scala", "org.intellij.scala", None)

ideaBuild := Versions.ideaVersion

onLoad in Global := ((s: State) => {
  "updateIdea" :: s
}) compose (onLoad in Global).value



lazy val root = newProject("RemoteRunPlugin", file("."))
  .dependsOn(
    zeppelin % "test->test;compile->compile",
    scalaIntegration % "test->test;compile->compile")
  .aggregate(
    zeppelin,
    scalaIntegration)
  .settings(
    aggregate.in(updateIdea) := false)


lazy val zeppelin = newProject("zeppelin", file("plugin/zeppelin"))
  .settings(
    version := Versions.scalaVersion,

    libraryDependencies ++= Dependencies.zeppelinDependencies,
    unmanagedJars in Compile += file(System.getProperty("java.home")).getParentFile / "lib" / "tools.jar",
  )
  .enablePlugins(SbtIdeaPlugin)

lazy val scalaIntegration =
  newProject("scala-intergration", file("plugin/integration/scala-plugin"))
    .dependsOn(zeppelin % "test->test;compile->compile")
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      ideaExternalPlugins += IdeaPlugin.Id("Scala", "org.intellij.scala", None),
    )
