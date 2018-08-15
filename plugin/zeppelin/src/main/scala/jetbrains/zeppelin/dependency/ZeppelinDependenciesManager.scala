package jetbrains.zeppelin.dependency

import com.intellij.openapi.roots.libraries.LibraryTable
import com.intellij.openapi.roots.{ModuleRootManager, OrderRootType}
import com.intellij.openapi.vfs.{JarFileSystem, VirtualFileManager}
import coursier.{Dependency, Module}
import jetbrains.zeppelin.models.SparkVersion

/**
  * A manager which downloads dependencies and creates descriptors
  */
object ZeppelinDependenciesManager {
  private val USER_LIBRARY = "UserInterpreterDependencies"

  /**
    * Get default Zeppelin dependencies
    *
    * @param zeppelinVersion - a version of Zeppelin
    * @return a library descriptor with a default Zeppelin dependencies
    */
  def getZeppelinSdkDescriptor(zeppelinVersion: String, sparkVersion: SparkVersion): LibraryDescriptor = {
    if (zeppelinVersion != "0.8.0") throw new Exception("This version of Zeppelin is not supported")

    val version = ZeppelinDependenciesVersions("2.11", sparkVersion.versionString)

    val dependencies = DefaultZeppelinDependencies.getDefaultZeppelinDependencies(version)
    val downloadedJars = Downloader.downloadDependencies(dependencies)
    val urls = downloadedJars.map(it => constructUrlString(it))
    LibraryDescriptor(zeppelinVersion, classes = urls)
  }

  /**
    * Import dependencies from Zeppelin to an IDEA module
    *
    * @param module       - module, where a library with dependencies will be created
    * @param dependencies - dependencies, which must be imported
    */
  def importUserInterpreterLibrary(module: com.intellij.openapi.module.Module, dependencies: List[String]): Unit = {
    val moduleModel = ModuleRootManager.getInstance(module).getModifiableModel
    val table: LibraryTable = moduleModel.getModuleLibraryTable
    val oldLibrary = Option(table.getLibraryByName(USER_LIBRARY))
    oldLibrary.foreach(it => {
      table.removeLibrary(it)
    })

    val library = table.createLibrary(USER_LIBRARY)
    val libraryModel = library.getModifiableModel

    val jars = resolveDependencies(dependencies)
    jars.foreach(it => {
      libraryModel.addRoot(it, OrderRootType.CLASSES)
    })

    libraryModel.commit()
    moduleModel.commit()
  }

  private def constructUrlString(srcFilePath: String): String = {
    VirtualFileManager
      .constructUrl(JarFileSystem.PROTOCOL, srcFilePath) + JarFileSystem.JAR_SEPARATOR
  }

  private def isFromRepo(dependency: String): Boolean = {
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
    val jars = downloadedJars ++ dependencies.filter(it => !isFromRepo(it))
    jars.map(it => constructUrlString(it))
  }
}