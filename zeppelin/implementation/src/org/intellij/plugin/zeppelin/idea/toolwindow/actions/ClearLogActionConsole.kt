package org.intellij.plugin.zeppelin.idea.toolwindow.actions

import com.intellij.execution.ui.ConsoleView
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAwareAction

/**
 * Console action to clear logs
 *
 * @param console - console that will be cleaned
 */
open class ClearLogActionConsole(var console: ConsoleView) :
        DumbAwareAction("Clear All", "Clear the contents of the zeppelin logs", AllIcons.Actions.GC) {
    override fun actionPerformed(e: AnActionEvent) {
        console.clear()
    }

    override fun update(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null && editor.document.textLength > 0
    }
}