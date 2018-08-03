package jetbrains.zeppelin.wizard

import java.io.File

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.libraries.LibraryTable
import com.intellij.openapi.roots.{ModuleRootManager, OrderRootType}
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

  def addAdditionalLibrary(module: com.intellij.openapi.module.Module, jars: List[String]): Unit = {
    val moduleModel = ModuleRootManager.getInstance(module).getModifiableModel
    val table: LibraryTable = moduleModel.getModuleLibraryTable
    val oldLibrary = Option(table.getLibraryByName("AdditionalDependencies"))
    oldLibrary.foreach(it => table.removeLibrary(it))
    if (oldLibrary.nonEmpty) {
      moduleModel.commit()
      return
    }


    val library = table.createLibrary("AdditionalDependencies")
    val libraryModel = library.getModifiableModel

    val urls = jars.map(it => constructUrlString(it))
    urls.foreach(it => {
      libraryModel.addRoot(it, OrderRootType.CLASSES)
      libraryModel.addRoot(it, OrderRootType.SOURCES)
    })
    ApplicationManager.getApplication.runWriteAction(new Runnable() {
      override def run(): Unit = {
        libraryModel.commit()
        moduleModel.commit()
      }
    })
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
