package jetbrains.zeppelin.api.websocket

import java.net.URI

import jetbrains.zeppelin.api
import jetbrains.zeppelin.api.ConnectionStatus.ConnectionStatus
import jetbrains.zeppelin.api._
import jetbrains.zeppelin.api.websocket.WebSocketApiProtocol._
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations._
import org.eclipse.jetty.websocket.client.{ClientUpgradeRequest, WebSocketClient}
import spray.json.{JsObject, _}

import scala.collection.mutable

//noinspection LoopVariableNotUpdated,RedundantBlock
@WebSocket(maxTextMessageSize = 64 * 1024)
class WebSocketAPI(uri: String) {
  private val client: WebSocketClient = new WebSocketClient()
  private val handlersMap: mutable.Map[String, MessageHandler] = mutable.Map()
  private val monitor = AnyRef
  var defaultHandler: MessageHandler = (_: ResponseMessage) => {
    println("Default Handler is called")
  }
  private var session: Session = _
  var status: api.ConnectionStatus.Value = ConnectionStatus.DISCONNECTED

  @OnWebSocketClose
  def onClose(statusCode: Int, reason: String): Unit = {
    status = ConnectionStatus.DISCONNECTED
  }

  @OnWebSocketError
  def onError(throwable: Throwable): Unit = {
    monitor.synchronized {
      status = ConnectionStatus.FAILED
      monitor.notifyAll()
    }
  }

  @OnWebSocketConnect
  def onConnect(session: Session) {
    this.session = session
    monitor.synchronized {
      status = ConnectionStatus.CONNECTED
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


  def connect(): ConnectionStatus = {
    val echoUri = new URI(uri)

    client.start()
    val request = new ClientUpgradeRequest()
    client.connect(this, echoUri, request)
    while (status == ConnectionStatus.DISCONNECTED) {
      waitMonitor()
    }
    status
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
    if (status != ConnectionStatus.CONNECTED) throw SessionIsClosedException()


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
    if (status != ConnectionStatus.CONNECTED) throw SessionIsClosedException()

    for (tuple <- handlersMap) registerHandler(tuple._1, tuple._2)

    import WebSocketApiProtocol._
    val msg = requestMessage.toJson.toString()
    session.getRemote.sendString(msg)
  }


  def registerHandler(op: String, handler: MessageHandler) {
    handlersMap += (op -> handler)
  }
}