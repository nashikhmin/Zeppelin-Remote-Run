package jetbrains.zeppelin.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.{DumbAwareAction, Project}
import jetbrains.zeppelin.api.idea.IdeaEditorApi
import jetbrains.zeppelin.components.ZeppelinConnection

/**
  * Starts or restarts sbt shell depending on running state.
  */
class RunCodeAction(project: Project) extends DumbAwareAction with IdeaEditorApi {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.Actions.Execute)
  templatePresentation.setText("Execute selected code")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val editor = currentEditor(event)
    val selectedText = currentSelectedText(editor)

    val zeppelinService = ZeppelinConnection.connectionFor(event.getProject).service
    zeppelinService.runCode(selectedText, s"${event.getProject.getName}")
  }
}
