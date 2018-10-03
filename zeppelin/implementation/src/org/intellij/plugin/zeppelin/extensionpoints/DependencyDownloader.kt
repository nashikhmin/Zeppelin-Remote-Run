package org.intellij.plugin.zeppelin.extensionpoints

import com.intellij.openapi.extensions.ExtensionPointName
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.dependency.Dependency
import org.intellij.plugin.zeppelin.models.ZeppelinException

/**
 * Extension point which implements dependency downloads
 */
interface DependencyDownloader {
    companion object {
        fun getAvailable() :DependencyDownloader = DependencyDownloader.EP_NAME.extensions.firstOrNull() ?: throw ZeppelinException(
                "Dependency downloader is not implemented")

        private val ID: String = ZeppelinConstants.PLUGIN_ID + ".dependencyDownloader"
        private val EP_NAME: ExtensionPointName<DependencyDownloader> = ExtensionPointName.create<DependencyDownloader>(
                ID)
    }

    /**
     * Download a dependency to local machine
     * @param dependencies - a dependency for downloading
     */
    fun resolveDependency(dependencies: List<Dependency>,logger: DependencyResolverLogger): List<String>
}

interface DependencyResolverLogger {
    fun printMessage(msg: String)
}
