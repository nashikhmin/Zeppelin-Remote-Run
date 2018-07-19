package jetbrains.zeppelin.ui.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.DumbAwareAction
import jetbrains.zeppelin.components.ZeppelinConnection
import jetbrains.zeppelin.ui.interpreter.InterpreterSettingsDialog

/**
  * Refresh a list of available interpreters on Zeppelin.
  */
class OpenInterpreterSettingsAction() extends DumbAwareAction {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.General.Settings)
  templatePresentation.setText("Open interpreter settings panel")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val connection = ZeppelinConnection.connectionFor(event.getProject)
    val interpretersView = connection.interpretersView
    val interpreterName = interpretersView.getSelectedValue
    val service = connection.service
    val interpreter = service.getInterpreterByName(event.getProject.getName, interpreterName)
    new InterpreterSettingsDialog(event.getProject, interpreter).show()
  }
}