package jetbrains.zeppelin.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.api.idea.IdeaEditorApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult}
import jetbrains.zeppelin.components.ZeppelinConnection

class SelectTextAction extends AnAction with IdeaEditorApi {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val connection = ZeppelinConnection.connectionFor(event.getProject)
    val zeppelinService = connection.service
    val editor = currentEditor(event)
    val selectedText = currentSelectedText(editor)

    connection.printMessage(s"Run paragraph with text: $selectedText...")
    zeppelinService.runCode(selectedText, NotificationHandlers(connection))
  }

  private def NotificationHandlers(connection: ZeppelinConnection): OutputHandler = {
    new OutputHandler {
      override def onError(): Unit = {
        connection.printError("Paragraph Run Error")
      }

      override def handle(result: OutputResult, isAppend: Boolean): Unit = {
        if (result.data.isEmpty)
          return
        connection.printMessage(result.data)
      }

      override def onSuccess(): Unit = {
        connection.printMessage("Paragraph is completed")
      }
    }
  }
}