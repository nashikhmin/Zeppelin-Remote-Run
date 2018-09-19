package org.intellij.plugin.zeppelin.extensionpoints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants

/**
  * An extension point for creating templates
  */
interface TemplateFileCreator {
    /**
     * Create a template
     * @param sourceFolder - a full path to source folder
     * @param project - an IDEA project
     */
    fun create(sourceFolder: String, project: Project)

    companion object {
        private val ID: String = ZeppelinConstants.PLUGIN_ID + ".templateFileCreator"
        private val EP_NAME: ExtensionPointName<TemplateFileCreator> = ExtensionPointName.create<TemplateFileCreator>(ID)
        fun get() = EP_NAME.extensions.firstOrNull()
    }
}