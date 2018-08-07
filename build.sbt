import Common._
import org.jetbrains.sbtidea.Keys.{ideaExternalPlugins, updateIdea}

onLoad in Global := ((s: State) => {
  "updateIdea" :: s
}) compose (onLoad in Global).value


ideaDownloadDirectory in ThisBuild := homePrefixDir / ".RemoteRunPlugin" / "sdk"
ideaExternalPlugins += scalaPlugin
ideaBuild := Versions.ideaVersion


val homePrefixDir = sys.props.get("tc.idea.prefix").map(new File(_)).getOrElse(Path.userHome)
//val scalaPlugin = IdeaPlugin.Id("Scala", "org.intellij.scala", Option("nightly"))
val scalaPlugin = IdeaPlugin.Zip("Scala", url("file:///home/nashikhmin/Downloads/scala-intellij-bin-2018.2.668.zip"))
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
  newProject("scala-integration", file("plugin/integration/scala-plugin"))
    .dependsOn(zeppelin % "test->test;compile->compile")
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      ideaExternalPlugins += scalaPlugin
    )
