package jetbrains.zeppelin.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.api.idea.IdeaEditorApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult}
import jetbrains.zeppelin.components.ZeppelinConnection
import jetbrains.zeppelin.utils.ZeppelinLogger

class SelectTextAction extends AnAction with IdeaEditorApi {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val zeppelinService = ZeppelinConnection.connectionFor(event.getProject).service
    if (!zeppelinService.isConnected) {
      ZeppelinLogger.printError("The action cannot be performed. Reconnect plugin to the server.")
      return
    }
    val editor = currentEditor(event)
    val selectedText = currentSelectedText(editor)

    ZeppelinLogger.printMessage(s"Run paragraph with text: $selectedText...")
    zeppelinService.runCode(selectedText, NotificationHandlers(), s"${event.getProject.getName}")
  }

  private def NotificationHandlers(): OutputHandler = {
    new OutputHandler {
      override def onError(): Unit = {
        ZeppelinLogger.printError("Paragraph Run Error")
      }

      override def handle(result: OutputResult, isAppend: Boolean): Unit = {
        if (result.data.isEmpty)
          return
        ZeppelinLogger.printMessage(result.data)
      }

      override def onSuccess(): Unit = {
        ZeppelinLogger.printMessage("Paragraph is completed")
      }
    }
  }
}