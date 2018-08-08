package jetbrains.zeppelin.idea.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.DumbAwareAction
import jetbrains.zeppelin.dependency.ImportZeppelinInterpreterDependencies

/**
  * Refresh a list of available interpreters on Zeppelin.
  */
class UpdateDependenciesAction extends DumbAwareAction {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.Actions.Download)
  templatePresentation.setText("Update Dependencies")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val importer = ImportZeppelinInterpreterDependencies(project = event.getProject)
    importer.invoke()
  }
}