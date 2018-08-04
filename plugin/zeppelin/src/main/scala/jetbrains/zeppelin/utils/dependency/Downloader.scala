package jetbrains.zeppelin.utils.dependency

import java.io.File

import coursier.maven.MavenRepository
import coursier.{Cache, Dependency, Fetch, FileError, Resolution}
import scalaz.\/
import scalaz.concurrent.Task

/**
  * Implement methods which can resolve dependencies
  */
object Downloader {
  /**
    * Download dependencies and get jars
    *
    * @param dependencies - dependencies which must be downloaded
    * @return a list of jar paths
    */
  def downloadDependencies(dependencies: Set[Dependency]): List[String] = {
    val start: Resolution = Resolution(
      dependencies
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