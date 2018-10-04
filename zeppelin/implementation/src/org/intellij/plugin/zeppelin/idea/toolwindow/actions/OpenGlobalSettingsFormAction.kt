package org.intellij.plugin.zeppelin.idea.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class OpenGlobalSettingsFormAction :
        DumbAwareAction("Settings", "Open Zeppelin global settings", AllIcons.General.Settings) {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Zeppelin Remote Run")
    }
}