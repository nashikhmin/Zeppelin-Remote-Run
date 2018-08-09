package jetbrains.zeppelin.idea.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.DumbAwareAction
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.idea.settings.interpreter.UpdateInterpreterHandler

/**
  * Set a new default interpreter for the Zeppelin
  */
class SetDefaultInterpretersAction extends DumbAwareAction {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.Actions.SetDefault)
  templatePresentation.setText("Set interpreter as a default")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val connection = ZeppelinComponent.connectionFor(event.getProject)
    val interpretersView = connection.interpretersView
    val interpreterName = interpretersView.getSelectedValue
    val service = connection.service
    service.setDefaultInterpreter(interpreterName)
    connection.updateInterpreterList()
    UpdateInterpreterHandler.getAll.foreach(_.updateInterpreter(event.getProject))
  }
}