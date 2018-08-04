package jetbrains.zeppelin.utils.dependency

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.libraries.LibraryTable
import com.intellij.openapi.roots.{ModuleRootManager, OrderRootType}
import com.intellij.openapi.vfs.{JarFileSystem, VirtualFileManager}
import coursier.{Dependency, Module}

/**
  * Manager which downloads dependencies and creates descriptors
  */
object ZeppelinDependenciesManager {
  private val USER_LIBRARY = "UserInterpreterDependencies"

  def addUserInterpreterLibrary(module: com.intellij.openapi.module.Module, dependencies: List[String]): Unit = {
    val jars = resolveDependencies(dependencies)
    val moduleModel = ModuleRootManager.getInstance(module).getModifiableModel
    val table: LibraryTable = moduleModel.getModuleLibraryTable
    val oldLibrary = Option(table.getLibraryByName(USER_LIBRARY))
    oldLibrary.foreach(it => {
      table.removeLibrary(it)
    })

    val library = table.createLibrary(USER_LIBRARY)
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

  def getZeppelinSdkDescriptor(version: String): LibraryDescriptor = {
    val depPaths = getDefaultDependencies(version)
    val urls = depPaths.map(it => constructUrlString(it))
    LibraryDescriptor(version, urls)
  }

  def isFromRepo(dependency: String): Boolean = {
    dependency.split(":").length == 3
  }

  private def resolveDependencies(dependencies: List[String]): List[String] = {
    val fromRepo = dependencies.filter(it => isFromRepo(it))
    val dependenciesForDownloading = fromRepo.map(it => {
      it.split(":") match {
        case Array(group, name, version) => {
          Dependency(
            Module(group, name), version
          )
        }
        case _ => throw new Exception("Wrong Dependencies")
      }

    }).toSet
    val downloadedJars = Downloader.downloadDependencies(dependenciesForDownloading)
    downloadedJars ++ dependencies.filter(it => !isFromRepo(it))
  }

  private def constructUrlString(srcFilePath: String): String = {
    VirtualFileManager
      .constructUrl(JarFileSystem.PROTOCOL, srcFilePath) + JarFileSystem.JAR_SEPARATOR
  }

  private def getDefaultDependencies(version: String): List[String] = {
    if (version != "0.8.0") throw new Exception("This version of Zeppelin is not supported")

    val dependencies = Set(
      Dependency(
        Module("org.apache.zeppelin", "zeppelin-spark-dependencies"), version
      ),
      Dependency(
        Module("org.apache.zeppelin", "spark-interpreter"), version
      )
    )
    Downloader.downloadDependencies(dependencies)
  }
}