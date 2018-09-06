import Common._
import org.jetbrains.sbtidea.Keys.ideaExternalPlugins

ideaPluginName in ThisBuild := "RemoteRun"
ideaBuild := Versions.ideaVersion

val dataVizPlugin = IdeaPlugin.Zip("DataViz", url("file:///home/nashikhmin/Downloads/Data-Vis-1.0-SNAPSHOT.zip"))
val scalaPlugin = IdeaPlugin.Zip("Scala", url("file:///home/nashikhmin/Downloads/scala-intellij-bin-2018.2.807.zip"))

ideaExternalPlugins += scalaPlugin
ideaExternalPlugins += dataVizPlugin



lazy val RemoteRunPlugin: Project = newProject("RemoteRunPlugin", file("."))
  .dependsOn(
    zeppelin % "test->test;compile->compile",
    scalaIntegration % "test->test;compile->compile",
    dataVizIntegration % "test->test;compile->compile")

lazy val zeppelin: Project = newProject("zeppelin", file("plugin/zeppelin"))
  .settings(
    version := Versions.scalaVersion,
    packageMethod := PackagingMethod.MergeIntoOther(RemoteRunPlugin),
    libraryDependencies ++= Dependencies.zeppelinDependencies
  )

lazy val scalaIntegration =
  newProject("scala-integration", file("plugin/integration/scala-plugin"))
    .dependsOn(zeppelin % "test->test;compile->compile")
    .settings(
      ideaExternalPlugins += scalaPlugin
    )

lazy val dataVizIntegration =
  newProject("dataviz-integration", file("plugin/integration/dataviz-plugin"))
    .dependsOn(zeppelin % "test->test;compile->compile")
    .settings(
      ideaExternalPlugins += dataVizPlugin
    )


def createRunnerProject(from: ProjectReference, name: String): Project = {
  newProject(name, file(s"target/tools/$name"))
    .dependsOn(from % Provided)
    .settings(
      dumpDependencyStructure := null, // avoid cyclic dependencies on products task
      products := packagePlugin.in(from).value :: Nil,
      packageMethod := org.jetbrains.sbtidea.Keys.PackagingMethod.Skip(),
      unmanagedJars in Compile := ideaMainJars.value,
      unmanagedJars in Compile += file(System.getProperty("java.home")).getParentFile / "lib" / "tools.jar"
    )
}

lazy val ideaRunner = createRunnerProject(RemoteRunPlugin, "idea-runner")

