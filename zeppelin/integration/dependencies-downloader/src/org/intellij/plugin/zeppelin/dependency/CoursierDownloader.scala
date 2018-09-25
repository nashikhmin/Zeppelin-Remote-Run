package org.intellij.plugin.zeppelin.dependency

import java.io.File

import coursier.maven.MavenRepository
import coursier.{Cache, Fetch, FileError, Module, Resolution}
import org.intellij.plugin.zeppelin.extensionpoints.DependencyDownloader
import scalaz.\/
import scalaz.concurrent.Task

import scala.collection.JavaConverters._

/**
  * Implement methods which can resolve dependencies
  */
class CoursierDownloader extends DependencyDownloader {
  override def resolveDependency(dependency: Dependency): String = {
    val exlusions: Set[Exclusion] = dependency.getExcludes.asScala.toSet
    val coursierExlusions = exlusions.map(it => (it.getGroup, it.getId))
    val coursierDependency = coursier.Dependency(
      Module(dependency.getGroup, dependency.getId), dependency.getVersion, exclusions = coursierExlusions
    )
    downloadDependencies(Set(coursierDependency)).head

  }

  /**
    * Download dependencies and get jars
    *
    * @param dependencies - dependencies which must be downloaded
    * @return a list of jar paths
    */
  private def downloadDependencies(dependencies: Set[coursier.Dependency]): List[String] = {
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
      .toList
  }
}