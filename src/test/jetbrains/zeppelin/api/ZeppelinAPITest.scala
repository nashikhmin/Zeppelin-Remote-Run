package jetbrains.zeppelin.api

import org.scalatest.{FunSuite, Matchers}
import spray.json.JsObject

import scala.util.Random

class ZeppelinAPITest extends FunSuite with Matchers {
  test("Zeppelin.createNotebookAndRunParagraph") {
    val restAPI = new RestAPI("localhost", 8080)
    val zeppelinAPI = new ZeppelinApi(restAPI)
    val credentials = zeppelinAPI.login("user1", "password2")
    val notebook = zeppelinAPI.createNotebook(s"RemoteNotebooks/${Random.alphanumeric.take(10).mkString}")
    assert(notebook.id.length == 9)
    val paragraph = zeppelinAPI.createParagraph(notebook.id)
    assert(paragraph.paragraphId.nonEmpty)

    val webSocketAPI = new WebSocketAPI("ws://localhost:8080/ws")
    webSocketAPI.connect()
    val result: JsObject = webSocketAPI.getNote(credentials, notebook.id)
    println(result.prettyPrint)
    assert(result.fields.nonEmpty)
  }
}
