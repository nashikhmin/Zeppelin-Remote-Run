package org.intellij.plugin.zeppelin.extensionpoints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
 * Extension points to handle table outputs
 */
interface TableOutputHandler {
    fun handle(project: Project, msg: String)

    companion object {
        private const val ID: String = ZeppelinConstants.PLUGIN_ID + ".tableOutputHandler"
        private val EP_NAME: ExtensionPointName<TableOutputHandler> = ExtensionPointName.create<TableOutputHandler>(
                ID)

        fun getHandler(): TableOutputHandler = EP_NAME.extensions.firstOrNull()?:DefaultTableOutputHandler()
    }
}

class DefaultTableOutputHandler : TableOutputHandler {
    override fun handle(project: Project, msg: String): Unit = ZeppelinLogger.printMessage(msg)
}