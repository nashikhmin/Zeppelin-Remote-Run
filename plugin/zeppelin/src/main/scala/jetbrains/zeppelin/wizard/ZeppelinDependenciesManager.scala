package jetbrains.zeppelin.wizard

import java.io.File

import com.intellij.openapi.vfs.{JarFileSystem, VirtualFileManager}
import coursier.maven.MavenRepository
import coursier.{Cache, Dependency, Fetch, FileError, Module, Resolution}
import scalaz.\/
import scalaz.concurrent.Task

/**
  * Manager which downloads dependencies and creates descriptors
  */
object ZeppelinDependenciesManager {
  def getZeppelinSdkDescriptor(version: String): ZeppelinSdkDescriptor = {
    val depPaths = downloadDefaultDependencies(version)
    val urls = depPaths.map(it => constructUrlString(it))
    ZeppelinSdkDescriptor(version, urls)
  }

  private def constructUrlString(srcFilePath: String): String = {
    VirtualFileManager
      .constructUrl(JarFileSystem.PROTOCOL, srcFilePath) + JarFileSystem.JAR_SEPARATOR
  }


  private def downloadDefaultDependencies(version: String): List[String] = {
    if (version != "0.8.0") throw new Exception("This version of Zeppelin is not supported")

    val start: Resolution = Resolution(
      Set(
        Dependency(
          Module("org.apache.zeppelin", "zeppelin-spark-dependencies"), version
        ),
        Dependency(
          Module("org.apache.zeppelin", "spark-interpreter"), version
        )
      )
    )

    val repositories = Seq(
      Cache.ivy2Local,
      MavenRepository("https://repo1.maven.org/maven2")
    )

    val fetch = Fetch.from(repositories, Cache.fetch())
    val resolution = start.process.run(fetch).unsafePerformSync

    val localArtifacts: Seq[FileError \/ File] = Task.gatherUnordered(
      resolution.artifacts.map(Cache.file(_).run)
    ).unsafePerformSync

    localArtifacts.map(_.getOrElse(throw new Exception()).getAbsolutePath)
      .filter(_.endsWith(".jar"))
      .filter(it => !it.contains("slf4j") && !it.contains("log4j"))
      .toList
  }
}
