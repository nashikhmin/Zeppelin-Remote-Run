package org.intellij.plugin.zeppelin.api.websocket

import java.net.URI

import com.intellij.openapi.diagnostic.Logger
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations._
import org.eclipse.jetty.websocket.client.{ClientUpgradeRequest, WebSocketClient}
import org.intellij.plugin.zeppelin.api.websocket.WebSocketApiProtocol._
import org.intellij.plugin.zeppelin.models
import org.intellij.plugin.zeppelin.models.ConnectionStatus.ConnectionStatus
import org.intellij.plugin.zeppelin.models._
import spray.json.{JsObject, _}

import scala.collection.mutable

//noinspection LoopVariableNotUpdated,RedundantBlock
@WebSocket(maxTextMessageSize = 1024 * 1024)
class WebSocketAPI(uri: String) {
  private val LOG = Logger.getInstance(getClass)
  private val client: WebSocketClient = new WebSocketClient()
  private val handlersMap: mutable.Map[String, MessageHandler] = mutable.Map()
  private val monitor = AnyRef
  var defaultHandler: MessageHandler = (msg: ResponseMessage) => {
    println(s"Default Handler is called. Operation: ${msg.op}. Data: ${msg.data}")
  }
  var status: models.ConnectionStatus.Value = ConnectionStatus.DISCONNECTED
  private var session: Session = _

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
    if (LOG.isTraceEnabled) LOG.trace(s"The message is handled $response.")
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

    doRequestWithoutWaitingResult(requestMessage)

    monitor.synchronized {
      while (!gotResponse && status == ConnectionStatus.CONNECTED) {
        monitor.wait()
      }
    }
    response.data
  }

  def doRequestWithoutWaitingResult(requestMessage: RequestMessage): Unit = {
    if (status != ConnectionStatus.CONNECTED) throw SessionIsClosedException()

    import WebSocketApiProtocol._
    val msg = requestMessage.toJson.toString()
    session.getRemote.sendString(msg)
  }

  def registerHandler(op: String, handler: MessageHandler) {
    handlersMap += (op -> handler)
  }

  def doRequestAsync(requestMessage: RequestMessage, handlersMap: Map[String, MessageHandler]): Unit = {
    for (tuple <- handlersMap) registerHandler(tuple._1, tuple._2)
    doRequestWithoutWaitingResult(requestMessage)
  }
}