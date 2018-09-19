package org.intellij.plugin.zeppelin.extensionpoints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants

/**
 * Updating after changing interpreters
 */
interface UpdateInterpreterHandler {
    fun updateInterpreter(project: Project)

    companion object {
        private val EP_NAME: ExtensionPointName<UpdateInterpreterHandler> = ExtensionPointName.create<UpdateInterpreterHandler>(
                ZeppelinConstants.PLUGIN_ID + ".updateInterpreterHandler")

        fun getAll(): Array<out UpdateInterpreterHandler> = EP_NAME.extensions
    }
}