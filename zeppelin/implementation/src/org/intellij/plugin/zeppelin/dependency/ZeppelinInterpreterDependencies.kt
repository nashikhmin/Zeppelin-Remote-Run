package org.intellij.plugin.zeppelin.dependency.dependency

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.LibraryTable
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import org.intellij.plugin.zeppelin.api.idea.IdeaCommonApi
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.dependency.DefaultZeppelinDependencies
import org.intellij.plugin.zeppelin.dependency.Dependency
import org.intellij.plugin.zeppelin.dependency.ZeppelinDependenciesVersions
import org.intellij.plugin.zeppelin.extensionpoints.DependencyDownloader
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.models.ZeppelinException
import org.intellij.plugin.zeppelin.utils.invokeLater

/**
 * Class which import user dependencies for a Zeppelin interpreter
 *
 * @param project - a current project
 */
class ZeppelinInterpreterDependencies(private val project: Project) {
    private val USER_LIBRARY = "UserInterpreterDependencies"

    companion object {
        /**
         * Get default Zeppelin dependencies
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

            val urls = dependencies.map {
                val downloader = DependencyDownloader.getAvailable()
                val path = downloader.resolveDependency(it)
                constructUrlString(path)
            }
            return LibraryDescriptor(zeppelinVersion, urls)
        }

        private fun constructUrlString(srcFilePath: String): String {
            return VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, srcFilePath) + JarFileSystem.JAR_SEPARATOR
        }
    }

    fun invokeImportUserDependencies() {
        val module = IdeaCommonApi.getCurrentModule(project) ?: return
        val jars: List<String> = getInterpreterUserDependenciesList()
        invokeLater {
            runWriteAction {
                importUserInterpreterLibrary(module, jars)
            }
        }
    }

    /**
     * Import dependencies from Zeppelin to an IDEA module
     *
     * @param module       - module, where a library with dependencies will be created
     * @param rawDependencies - dependencies, which must be imported
     */
    private fun importUserInterpreterLibrary(module: Module, rawDependencies: List<String>) {
        val moduleModel = ModuleRootManager.getInstance(module).modifiableModel
        val table: LibraryTable = moduleModel.moduleLibraryTable;
        table.getLibraryByName(USER_LIBRARY)?.let { table.removeLibrary(it) }

        val library = table.createLibrary(USER_LIBRARY)
        val libraryModel = library.modifiableModel

        rawDependencies.forEach {
            val jar = resolveDependency(it)
            libraryModel.addRoot(jar, OrderRootType.CLASSES)
        }

        libraryModel.commit()
        moduleModel.commit()
    }

    private fun resolveDependency(rawDependency: String): String =
            if (isFromRepo(rawDependency)) {
                rawDependency
            } else {
                val parts = rawDependency.split(":")
                val dependency = Dependency(parts[0], parts[1], parts[2])
                val resolvedDependency = DependencyDownloader.getAvailable().resolveDependency(dependency)
                constructUrlString(resolvedDependency)
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