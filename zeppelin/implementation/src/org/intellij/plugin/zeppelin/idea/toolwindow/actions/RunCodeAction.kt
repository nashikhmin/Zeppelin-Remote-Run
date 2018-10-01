package org.intellij.plugin.zeppelin.idea.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import org.intellij.plugin.zeppelin.api.idea.IdeaEditorApi
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
 * Execute selected code on Zeppelin
 */
class RunCodeAction : DumbAwareAction("Execute selected",
        "Execution selected code in Zeppelin", AllIcons.Actions.Execute) {
    override fun actionPerformed(event: AnActionEvent) {
        val selectedText: String = IdeaEditorApi.currentEditor(event)?.let { IdeaEditorApi.currentSelectedText(it) }
                ?: ""
        val project = event.project
        if (project == null) {
            ZeppelinLogger.printError(ZeppelinConstants.ERROR_PROJECT_IS_NOT_SPECIFIED)
            return
        }
        val service = ZeppelinComponent.connectionFor(project).service
        service.runCode(selectedText, null)
    }
}