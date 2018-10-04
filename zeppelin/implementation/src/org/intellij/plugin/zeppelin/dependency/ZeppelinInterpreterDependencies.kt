package org.intellij.plugin.zeppelin.dependency

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import org.intellij.plugin.zeppelin.api.idea.IdeaCommonApi
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.extensionpoints.DependencyDownloader
import org.intellij.plugin.zeppelin.extensionpoints.DependencyResolverLogger
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.models.ZeppelinException

/**
 * Class which import user dependencies for a Zeppelin interpreter
 *
 * @param project - a current project
 */
class ZeppelinInterpreterDependencies(private val project: Project) {
    companion object {
        private const val USER_LIBRARY = "UserInterpreterDependencies"

        /**
         * Get a descriptor with default Zeppelin dependencies
         *
         * @param zeppelinVersion   - a version of Zeppelin
         * @param progressIndicator - an indicator of downloading progress
         * @return a library descriptor with a default Zeppelin dependencies
         */
        fun getZeppelinSdkDescriptor(zeppelinVersion: String, sparkVersion: SparkVersion,
                                     progressIndicator: ProgressIndicator): LibraryDescriptor {
            if (zeppelinVersion != "0.8.0") throw ZeppelinException("This version of Zeppelin is not supported")
            val version = ZeppelinDependenciesVersions("2.11", sparkVersion.versionString, zeppelinVersion)
            progressIndicator.text = "Get list of dependencies..."
            val dependencies = DefaultZeppelinDependencies.getDefaultZeppelinDependencies(version)
            progressIndicator.text = "Downloading dependencies..."

            val paths = downloadDependencies(progressIndicator, dependencies)
            val urls = paths.map { constructUrlString(it) }
            return LibraryDescriptor(zeppelinVersion, urls)
        }

        private fun downloadDependencies(progressIndicator: ProgressIndicator,
                                         dependencies: List<Dependency>): List<String> {
            val logger = object : DependencyResolverLogger {
                override fun printMessage(msg: String) {
                    progressIndicator.text2 = msg
                }
            }
            val downloader = DependencyDownloader.getAvailable()
            return downloader.resolveDependency(dependencies, logger)
        }

        private fun constructUrlString(srcFilePath: String): String {
            return VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, srcFilePath) + JarFileSystem.JAR_SEPARATOR
        }
    }

    /**
     * Start import of user dependencies in module dependencies
     */
    fun invokeImportUserDependencies() {
        val module = IdeaCommonApi.getCurrentModule(project) ?: return

        var jars: List<String> = listOf()
        val manager: ProgressManager = ProgressManager.getInstance()
        val executionResult = manager.runProcessWithProgressSynchronously(
                {
                    val progressIndicator = manager.progressIndicator
                    jars = resolveRawDependency(getInterpreterUserDependenciesList(), progressIndicator)
                },
                "Import User dependencies for interpreter", false, null)

        if (!executionResult) return
        updateUserLibraryFromJar(module, jars)
    }

    private fun updateUserLibraryFromJar(module: Module,
                                         jars: List<String>) {
        val moduleModel =
                ModuleRootManager.getInstance(module).modifiableModel
        val table = moduleModel.moduleLibraryTable
        table.getLibraryByName(USER_LIBRARY)?.let { table.removeLibrary(it) }
        table.createLibrary(USER_LIBRARY)
        val libraryModel = table.getLibraryByName(USER_LIBRARY)?.modifiableModel ?: throw ZeppelinException(
                "It is not right behavior of a plugin")

        runWriteAction {
            jars.forEach { jar -> libraryModel.addRoot(jar, OrderRootType.CLASSES) }
            libraryModel.commit()
            moduleModel.commit()
        }
    }

    private fun resolveRawDependency(rawDependencies: List<String>,
                                     indicator: ProgressIndicator): List<String> {
        val (fromRepoDeps, localFilesDeps) = rawDependencies.partition { isFromRepo(it) }

        val depsForDownload = fromRepoDeps.map {
            val parts = it.split(":")
            Dependency(parts[0], parts[1], parts[2])
        }
        val resolvedDependencies = downloadDependencies(indicator, depsForDownload)
        return (resolvedDependencies + localFilesDeps).map { constructUrlString(it) }
    }

    private fun isFromRepo(dependency: String): Boolean =
            dependency.split(":").size == 3

    private fun getInterpreterUserDependenciesList(): List<String> {
        val connection = ZeppelinComponent.connectionFor(project)
        val service = connection.service
        val interpreter = service.getDefaultInterpreter() ?: return listOf()
        return interpreter.dependencies.map { it.groupArtifactVersion }
    }
}