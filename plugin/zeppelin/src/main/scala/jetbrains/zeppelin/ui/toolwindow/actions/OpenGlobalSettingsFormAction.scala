package jetbrains.zeppelin.ui.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction

class OpenGlobalSettingsFormAction() extends
  DumbAwareAction("Settings",
    "Open Zeppelin global settings",
    AllIcons.General.Settings) {
  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getProject
    ShowSettingsUtil.getInstance.showSettingsDialog(project, "Zeppelin Remote Run")
  }
}