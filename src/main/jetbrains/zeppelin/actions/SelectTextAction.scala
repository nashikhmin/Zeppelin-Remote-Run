package jetbrains.zeppelin.actions

import com.intellij.notification.{Notification, NotificationType, Notifications}
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.api.idea.IdeaEditorApi
import jetbrains.zeppelin.api.rest.{RestAPI, ZeppelinRestApi}
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult, WebSocketAPI, ZeppelinWebSocketAPI}
import jetbrains.zeppelin.api.{NewNotebook, NewParagraph, Notebook}

import scala.util.Random

class SelectTextAction extends AnAction with IdeaEditorApi {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val editor = currentEditor(event)

    val selectedText = currentSelectedText(editor)
    val newNote = getNewNote(selectedText)

    val restAPI = new RestAPI("localhost", 8080)
    val zeppelinAPI = new ZeppelinRestApi(restAPI)
    val credentials = zeppelinAPI.login("user1", "password2")

    val notebookRest = zeppelinAPI.createNotebook(newNote)
    assert(notebookRest.id.length == 9)

    val webSocketAPI = new WebSocketAPI("ws://localhost:8080/ws")
    webSocketAPI.connect()
    val zeppelinWebSocketAPI: ZeppelinWebSocketAPI = new ZeppelinWebSocketAPI(webSocketAPI)
    val notebookWS: Notebook = zeppelinWebSocketAPI.getNote(credentials, notebookRest.id)


    val handler = new OutputHandler {
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

    zeppelinWebSocketAPI.runParagraph(notebookWS.paragraphs.head, handler, credentials)
  }

  private def getNewNote(code: String): NewNotebook = {
    val notebookName = s"RemoteNotebooks/${Random.alphanumeric.take(10).mkString}"
    val newParagraph = NewParagraph("runCode", code)
    NewNotebook(notebookName, List(newParagraph))
  }
}