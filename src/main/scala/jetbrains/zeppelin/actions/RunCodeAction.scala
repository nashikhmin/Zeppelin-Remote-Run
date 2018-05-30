package jetbrains.zeppelin.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.api.idea.IdeaEditorApi
import jetbrains.zeppelin.components.ZeppelinConnection

class RunCodeAction extends AnAction with IdeaEditorApi {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val editor = currentEditor(event)
    val selectedText = currentSelectedText(editor)

    val zeppelinService = ZeppelinConnection.connectionFor(event.getProject).service
    zeppelinService.runCode(selectedText, s"${event.getProject.getName}")
  }
}