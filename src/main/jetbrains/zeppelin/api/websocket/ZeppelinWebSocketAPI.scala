package jetbrains.zeppelin.api.websocket

import jetbrains.zeppelin.api.ZeppelinAPIProtocol._
import jetbrains.zeppelin.api._
import spray.json.JsObject

class ZeppelinWebSocketAPI(webSocketAPI: WebSocketAPI) {
  def getNote(credentials: Credentials, noteId: String): Notebook = {
    val data = Map("id" -> noteId)
    val opRequest = "GET_NOTE"
    val opResponse = "NOTE"
    val requestMessage = RequestMessage(opRequest, data, credentials)
    val response = webSocketAPI.sendDataAndGetResponseData(opResponse, requestMessage).fields.getOrElse("note", JsObject())
    println(response.prettyPrint)
    response.convertTo[Notebook]
  }
}
