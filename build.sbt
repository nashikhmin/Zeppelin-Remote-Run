import Common._
import org.jetbrains.sbtidea.Keys.{ideaExternalPlugins, updateIdea}

onLoad in Global := ((s: State) => {
  "updateIdea" :: s
}) compose (onLoad in Global).value


val homePrefixDir = sys.props.get("tc.idea.prefix").map(new File(_)).getOrElse(Path.userHome)

val scalaPlugin = IdeaPlugin.Id("Scala", "org.intellij.scala", Option("nightly"))
val dataVizPlugin = IdeaPlugin.Zip("DataViz", url("file:///home/nashikhmin/Downloads/Data-Vis-1.0-SNAPSHOT.zip"))


ideaDownloadDirectory in ThisBuild := homePrefixDir / ".RemoteRunPlugin" / "sdk"
ideaExternalPlugins += scalaPlugin
ideaExternalPlugins += dataVizPlugin
ideaBuild := Versions.ideaVersion



lazy val root = newProject("RemoteRunPlugin", file("."))
  .dependsOn(
    zeppelin % "test->test;compile->compile",
    scalaIntegration % "test->test;compile->compile",
    dataVizIntegration % "test->test;compile->compile")
  .aggregate(
    zeppelin,
    scalaIntegration,
    dataVizIntegration)
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
  newProject("scala-integration", file("plugin/integration/scala-plugin"))
    .dependsOn(zeppelin % "test->test;compile->compile")
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      ideaExternalPlugins += scalaPlugin
    )

lazy val dataVizIntegration =
  newProject("dataviz-integration", file("plugin/integration/dataviz-plugin"))
    .dependsOn(zeppelin % "test->test;compile->compile")
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      ideaExternalPlugins += dataVizPlugin
    )

