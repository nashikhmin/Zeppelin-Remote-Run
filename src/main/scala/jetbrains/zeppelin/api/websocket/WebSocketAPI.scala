package jetbrains.zeppelin.api.websocket

import java.net.URI

import jetbrains.zeppelin.api._
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.{OnWebSocketClose, OnWebSocketConnect, OnWebSocketMessage, WebSocket}
import org.eclipse.jetty.websocket.client.{ClientUpgradeRequest, WebSocketClient}
import spray.json.{JsObject, _}

import scala.collection.mutable


case class SessionIsClosedException() extends Exception()


case class RequestMessage(op: String, data: JsValue, credentials: Credentials)

case class ResponseMessage(op: String, data: JsObject)

trait MessageHandler {
  def handle(result: ResponseMessage)
}

object WebSocketApiProtocol extends DefaultJsonProtocol {

  implicit object RequestMessageFormat extends RootJsonFormat[RequestMessage] {
    def write(r: RequestMessage): JsObject = {
      JsObject(
        "op" -> JsString(r.op),
        "data" -> r.data,
        "ticket" -> JsString(r.credentials.ticket),
        "principal" -> JsString(r.credentials.principal),
        "roles" -> JsString(r.credentials.roles)
      )
    }

    def read(value: JsValue): RequestMessage = {
      throw DeserializationException("Non implemented")
    }
  }


  implicit object ResponseMessageFormat extends RootJsonFormat[ResponseMessage] {
    def write(r: ResponseMessage): JsValue = {
      throw throw DeserializationException("Non implemented")
    }

    def read(value: JsValue): ResponseMessage = {
      value.asJsObject.getFields("op", "data") match {
        case Seq(JsString(op), data) => ResponseMessage(op, data.asJsObject())
        case _ => throw DeserializationException("Response message expected")
      }
    }
  }

}


import jetbrains.zeppelin.api.websocket.WebSocketApiProtocol._

@WebSocket(maxTextMessageSize = 64 * 1024)
class WebSocketAPI(uri: String) {

  private val handlersMap: mutable.Map[String, MessageHandler] = mutable.Map()
  private val monitor = AnyRef
  var defaultHandler: MessageHandler = (result: ResponseMessage) => {
    println("Default Handler is called")
  }
  private var session: Session = _
  private var isConnected = false
  private var client: WebSocketClient = _

  @OnWebSocketClose
  def onClose(statusCode: Int, reason: String) {}

  @OnWebSocketConnect
  def onConnect(session: Session) {
    this.session = session
    monitor.synchronized {
      isConnected = true
      monitor.notifyAll()
    }
  }

  @OnWebSocketMessage
  def onMessage(msg: String) {
    val json = msg.parseJson.asJsObject
    val response = json.convertTo[ResponseMessage]
    val operationCode = response.op
    handlersMap.getOrElse(operationCode, defaultHandler).handle(response)
  }

  def connect() {
    val echoUri = new URI(uri)
    client = new WebSocketClient()
    client.start()
    val request = new ClientUpgradeRequest()
    client.connect(this, echoUri, request)
    while (!isConnected) {
      waitMonitor()
    }
  }

  private def waitMonitor() {
    monitor.synchronized {
      monitor.wait()
    }
  }

  def notifyMonitor(): Unit = {
    monitor.synchronized {
      monitor.notifyAll()
    }
  }

  def close() {
    client.stop()
  }


  def doRequestSync(requestMessage: RequestMessage, responseCode: String): JsObject = {
    if (!isConnected) throw SessionIsClosedException()


    var gotResponse = false
    var response: ResponseMessage = null

    registerHandler(responseCode, (result: ResponseMessage) => {
      monitor.synchronized {
        response = result
        gotResponse = true
        monitor.notifyAll()
      }
    })

    import WebSocketApiProtocol._
    val msg = requestMessage.toJson.toString()

    session.getRemote.sendString(msg)
    monitor.synchronized {
      while (!gotResponse) {
        monitor.wait()
      }
    }
    response.data
  }

  def doRequestAsync(requestMessage: RequestMessage, handlersMap: Map[String, MessageHandler]) {
    if (!isConnected) throw SessionIsClosedException()

    for (tuple <- handlersMap) registerHandler(tuple._1, tuple._2)

    import WebSocketApiProtocol._
    val msg = requestMessage.toJson.toString()
    session.getRemote.sendString(msg)
  }


  def registerHandler(op: String, handler: MessageHandler) {
    handlersMap += (op -> handler)
  }
}
