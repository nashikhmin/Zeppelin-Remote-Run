package jetbrains.zeppelin.ui.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.{DumbAwareAction, Project}
import jetbrains.zeppelin.api.idea.IdeaEditorApi
import jetbrains.zeppelin.components.ZeppelinComponent

/**
  * Execute selected code on Zeppelin
  */
class RunCodeAction(project: Project) extends DumbAwareAction with IdeaEditorApi {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.Actions.Execute)
  templatePresentation.setText("Execute selected code")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val editor = currentEditor(event)
    val selectedText = currentSelectedText(editor)

    val connection = ZeppelinComponent.connectionFor(event.getProject)
    val zeppelinService = connection.service
    zeppelinService.runCode(selectedText)
  }
}
