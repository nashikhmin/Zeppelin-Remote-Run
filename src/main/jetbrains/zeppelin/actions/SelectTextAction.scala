package jetbrains.zeppelin.actions

import com.intellij.notification.{Notification, NotificationType, Notifications}
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.api.idea.IdeaEditorApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult}
import jetbrains.zeppelin.components.ZeppelinConnection

class SelectTextAction extends AnAction with IdeaEditorApi {
  override def actionPerformed(event: AnActionEvent): Unit = {

    val zeppelinService = ZeppelinConnection.connectionFor(event.getProject).service
    val editor = currentEditor(event)
    val selectedText = currentSelectedText(editor)

    zeppelinService.runCode(selectedText, NotificationHandlers)
  }

  private def NotificationHandlers: OutputHandler = {
    new OutputHandler {
      override def onError(): Unit = {
        Notifications.Bus
          .notify(new Notification("Zeppelin Remote Run", " Zeppelin Remote Run:", "Paragraph Run Error", NotificationType
            .ERROR))
      }

      override def handle(result: OutputResult, isAppend: Boolean): Unit = {
        if (result.data.isEmpty)
          return

        Notifications.Bus
          .notify(new Notification("Zeppelin Remote Run", " Zeppelin Remote Run:", result.data, NotificationType
            .INFORMATION))
      }

      override def onSuccess(): Unit = {
        Notifications.Bus
          .notify(new Notification("Zeppelin Remote Run", " Zeppelin Remote Run:", "Paragraph Run Completed", NotificationType
            .INFORMATION))
      }
    }
  }
}