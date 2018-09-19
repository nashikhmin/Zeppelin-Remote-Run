package org.intellij.plugin.zeppelin.idea.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
 * Refresh a list of available interpreters on Zeppelin.
 */
class RefreshInterpretersAction :
        DumbAwareAction("Refresh", "Refresh Zeppelin connection", AllIcons.Actions.Refresh) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        if (project == null) {
            ZeppelinLogger.printError(ZeppelinConstants.ERROR_PROJECT_IS_NOT_SPECIFIED)
            return
        }
        ZeppelinComponent.connectionFor(project).updateInterpreterList(true)
    }
}