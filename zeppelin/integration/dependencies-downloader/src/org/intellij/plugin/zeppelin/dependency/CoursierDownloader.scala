package org.intellij.plugin.zeppelin.dependency

import java.io.File
import java.util

import coursier.Cache.Logger
import coursier.maven.MavenRepository
import coursier.{Cache, Fetch, FileError, Module, Resolution}
import org.intellij.plugin.zeppelin.extensionpoints.{DependencyDownloader, DependencyResolverLogger}
import scalaz.\/
import scalaz.concurrent.Task

import scala.collection.JavaConverters._

/**
  * Implement methods which can resolve dependencies
  */
class CoursierDownloader extends DependencyDownloader {
  override def resolveDependency(dependencies: util.List[Dependency], logger: DependencyResolverLogger): util.List[String] = {
    val coursierDependencies = dependencies.asScala.map(dependency => {
      val exlusions: Set[Exclusion] = dependency.getExcludes.asScala.toSet
      val coursierExlusions = exlusions.map(it => (it.getGroup, it.getId))
      coursier.Dependency(
        Module(dependency.getGroup, dependency.getId), dependency.getVersion, exclusions = coursierExlusions
      )
    }).toSet
    downloadDependencies(coursierDependencies, logger).asJava
  }

  /**
    * Download dependencies and get jars
    *
    * @param dependencies - dependencies which must be downloaded
    * @return a list of jar paths
    */
  private def downloadDependencies(dependencies: Set[coursier.Dependency],logger: DependencyResolverLogger): List[String] = {
    val start: Resolution = Resolution(
      dependencies
    )

    val repositories = Seq(
      Cache.ivy2Local,
      MavenRepository("https://repo1.maven.org/maven2")
    )

    Cache.fetch()
    val fetch = Fetch.from(repositories, Cache.fetch(logger = Some(new Logger {
      override def foundLocally(url: String, f: File): Unit = {
        logger.printMessage(s"Found locally $url")
      }
      override def downloadingArtifact(url: String, file: File): Unit = {
        logger.printMessage(s"Downloading $url")
      }
    })))
    val resolution = start.process.run(fetch).unsafePerformSync


    val localArtifacts: Seq[FileError \/ File] = Task.gatherUnordered(
      resolution.artifacts.map(Cache.file(_).run)
    ).unsafePerformSync

    localArtifacts.map(_.getOrElse(throw new Exception()).getAbsolutePath)
      .filter(_.endsWith(".jar"))
      .toList
  }
}