package jetbrains.zeppelin.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, PlatformDataKeys}
import com.intellij.openapi.ui.Messages
import jetbrains.zeppelin.api.rest.{RestAPI, ZeppelinRestApi}
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult, WebSocketAPI, ZeppelinWebSocketAPI}
import jetbrains.zeppelin.api.{NewNotebook, NewParagraph, Notebook}

import scala.util.Random

class TextBoxes extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val project = event.getData(PlatformDataKeys.PROJECT_CONTEXT)


    val restAPI = new RestAPI("localhost", 8080)
    val zeppelinAPI = new ZeppelinRestApi(restAPI)
    val credentials = zeppelinAPI.login("user1", "password2")

    val notebookRest = zeppelinAPI.createNotebook(getNewNote)
    assert(notebookRest.id.length == 9)

    val webSocketAPI = new WebSocketAPI("ws://localhost:8080/ws")
    webSocketAPI.connect()
    val zeppelinWebSocketAPI: ZeppelinWebSocketAPI = new ZeppelinWebSocketAPI(webSocketAPI)
    val notebookWS: Notebook = zeppelinWebSocketAPI.getNote(credentials, notebookRest.id)

    val monitor = AnyRef
    var waitResult = true
    var result = "none"
    var output = ""
    val handler = new OutputHandler {
      override def onError(): Unit = {
        monitor.synchronized {
          result = "error"
          waitResult = false
          monitor.notifyAll()
        }
      }

      override def handle(result: OutputResult, isAppend: Boolean): Unit = {
        output = output + result.data
      }

      override def onSuccess(): Unit = {
        monitor.synchronized {
          result = "success"
          waitResult = false
          monitor.notifyAll()
        }
      }
    }

    zeppelinWebSocketAPI.runParagraph(notebookWS.paragraphs.head, handler, credentials)
    monitor.synchronized {
      while (waitResult) {
        monitor.wait()
      }
    }
    if (result == "success") {
      Messages.showMessageDialog(project, s"Output: $output", "Success", Messages.getInformationIcon)
    }
    else {
      Messages.showMessageDialog(project, s"Problem during executing", "Error", Messages.getErrorIcon)
    }
  }

  private def getNewNote = {
    val runCode = "println(\"Hello, Jetbrains!\")"
    val notebookName = s"RemoteNotebooks/${Random.alphanumeric.take(10).mkString}"
    val newParagraph = NewParagraph("runCode", runCode)
    NewNotebook(notebookName, List(newParagraph))
  }
}