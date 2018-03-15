package jetbrains.zeppelin.api

import java.net.URI

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.{OnWebSocketClose, OnWebSocketConnect, OnWebSocketMessage, WebSocket}
import org.eclipse.jetty.websocket.client.{ClientUpgradeRequest, WebSocketClient}
import spray.json.DefaultJsonProtocol._
import spray.json.{JsObject, _}

import scala.collection.mutable


case class SessionIsClosedException() extends Exception()

trait MessageHandler {
  def handle(result: JsObject)
}

object WebSocketApiProtocol extends DefaultJsonProtocol {

  implicit object RequestMessageFormat extends RootJsonFormat[RequestMessage] {
    def write(r: RequestMessage): JsObject = {
      JsObject(
        "op" -> JsString(r.op), "data" -> r.data.toJson,
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
      value.asJsObject.getFields("data") match {
        case Seq(data) => ResponseMessage(data.asJsObject())
        case _ => throw DeserializationException("Response message expected")
      }
    }
  }

}


case class RequestMessage(op: String, data: Map[String, String], credentials: Credentials)

case class ResponseMessage(data: JsObject)

@WebSocket(maxTextMessageSize = 64 * 1024)
class WebSocketAPI(uri: String) extends LazyLogging {

  private val handlersMap: mutable.Map[String, MessageHandler] = mutable.Map()
  private val monitor = AnyRef
  var defaultHandler: MessageHandler = (result: JsObject) => {
    println("Can't handle " + result.toString())
  }
  private var session: Session = _
  private var isConnected = false
  private var client: WebSocketClient = _

  @OnWebSocketClose
  def onClose(statusCode: Int, reason: String) {
    logger.info(s"Connection closed: $statusCode - $reason")
  }

  @OnWebSocketConnect
  def onConnect(session: Session) {
    logger.info(s"Got connect: $session")
    this.session = session
    monitor.synchronized {
      isConnected = true
      monitor.notifyAll()
    }
  }

  @OnWebSocketMessage
  def onMessage(msg: String) {
    val json = msg.parseJson.asJsObject
    val operationCode = json.fields.getOrElse("op", "").toString.parseJson.convertTo[String]
    handlersMap.getOrElse(operationCode, defaultHandler).handle(json)
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

  def getNote(credentials: Credentials, noteId: String): JsObject = {
    val data = Map("id" -> noteId)
    val opRequest = "GET_NOTE"
    val opResponse = "NOTE"
    val requestMessage = RequestMessage(opRequest, data, credentials)
    sendDataAndGetResponseData(opResponse, requestMessage)
  }

  def sendDataAndGetResponseData(responseCode: String, requestMessage: RequestMessage): JsObject = {
    if (!isConnected) throw SessionIsClosedException()


    var gotResponse = false
    var response: JsObject = null

    registerHandler(responseCode, (result: JsObject) => {
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
    response.convertTo[ResponseMessage].data
  }

  def registerHandler(op: String, handler: MessageHandler) {
    handlersMap += (op -> handler)
  }
}
