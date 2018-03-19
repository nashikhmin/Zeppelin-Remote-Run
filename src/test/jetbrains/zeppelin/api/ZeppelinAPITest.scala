package jetbrains.zeppelin.api

import jetbrains.zeppelin.api.rest.{RestAPI, ZeppelinRestApi}
import jetbrains.zeppelin.api.websocket.{OutputHandler, OutputResult, WebSocketAPI, ZeppelinWebSocketAPI}
import org.scalatest.{FunSuite, Matchers}

import scala.util.Random

class ZeppelinAPITest extends FunSuite with Matchers {
  private val monitor = AnyRef
  test("Zeppelin.createNotebookAndRunParagraph") {
    val restAPI = new RestAPI("localhost", 8080)
    val zeppelinAPI = new ZeppelinRestApi(restAPI)
    val credentials = zeppelinAPI.login("user1", "password2")

    val notebookRest = zeppelinAPI.createNotebook(getNewNote)
    assert(notebookRest.id.length == 9)

    val webSocketAPI = new WebSocketAPI("ws://localhost:8080/ws")
    webSocketAPI.connect()
    val zeppelinWebSocketAPI: ZeppelinWebSocketAPI = new ZeppelinWebSocketAPI(webSocketAPI)
    val notebookWS: Notebook = zeppelinWebSocketAPI.getNote(credentials, notebookRest.id)


    var waitResult = true
    var result = "none"
    val handler = new OutputHandler {
      override def onError(): Unit = {
        monitor.synchronized {
          waitResult = false
          result = "fail"
          monitor.notifyAll()
        }
      }

      override def handle(result: OutputResult, isAppend: Boolean): Unit = {
        assert(result.data.isEmpty || result.data == "hello world\n")
      }

      override def onSuccess(): Unit = {
        monitor.synchronized {
          waitResult = false
          result = "success"
          monitor.notifyAll()
        }
      }
    }

    zeppelinWebSocketAPI.runParagraph(notebookWS.paragraphs.head, handler, credentials)
    monitor.synchronized {
      while (waitResult) {
        monitor.wait(20 * 1000)
      }
    }

    assert(result == "success")
  }

  private def getNewNote = {
    val runCode = "println(\"hello world\")"
    val notebookName = s"RemoteNotebooks/${Random.alphanumeric.take(10).mkString}"
    val newParagraph = NewParagraph("runCode", runCode)
    NewNotebook(notebookName, List(newParagraph))
  }
}
