package jetbrains.zeppelin.ui.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.DumbAwareAction
import jetbrains.zeppelin.components.ZeppelinComponent

/**
  * Refresh a list of available interpreters on Zeppelin.
  */
class RefreshInterpretersAction extends DumbAwareAction {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.Actions.Refresh)
  templatePresentation.setText("Refresh Interpreters list")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val connection = ZeppelinComponent.connectionFor(event.getProject)
    connection.updateInterpreterList()
  }
}