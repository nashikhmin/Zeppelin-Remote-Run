package jetbrains.zeppelin.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.DumbAwareAction
import jetbrains.zeppelin.components.ZeppelinConnection

/**
  * Refresh a list of available interpreters on Zeppelin.
  */
class RefreshInterpretersAction extends DumbAwareAction {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.Actions.Refresh)
  templatePresentation.setText("Refresh Interpreters list")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val connection = ZeppelinConnection.connectionFor(event.getProject)
    val service = connection.service
    val interpretersNames = service.interpreterList.map(_.name)
    connection.interpretersView.updateInterpretersList(interpretersNames)
  }
}