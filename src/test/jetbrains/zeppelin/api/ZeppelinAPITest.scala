package jetbrains.zeppelin.api

import jetbrains.zeppelin.api.rest.{RestAPI, ZeppelinRestApi}
import jetbrains.zeppelin.api.websocket.{WebSocketAPI, ZeppelinWebSocketAPI}
import org.scalatest.{FunSuite, Matchers}

import scala.util.Random

class ZeppelinAPITest extends FunSuite with Matchers {
  test("Zeppelin.createNotebookAndRunParagraph") {
    val restAPI = new RestAPI("localhost", 8080)
    val zeppelinAPI = new ZeppelinRestApi(restAPI)
    val credentials = zeppelinAPI.login("user1", "password2")
    val notebookRest = zeppelinAPI.createNotebook(s"RemoteNotebooks/${Random.alphanumeric.take(10).mkString}")
    assert(notebookRest.id.length == 9)
    val paragraph = zeppelinAPI.createParagraph(notebookRest.id)
    assert(paragraph.id.nonEmpty)

    val webSocketAPI = new WebSocketAPI("ws://localhost:8080/ws")
    webSocketAPI.connect()
    val zeppelinWebSocketAPI: ZeppelinWebSocketAPI = new ZeppelinWebSocketAPI(webSocketAPI)
    val notebookWS: Notebook = zeppelinWebSocketAPI.getNote(credentials, notebookRest.id)
    println(notebookWS.id == notebookRest.id)
  }
}
