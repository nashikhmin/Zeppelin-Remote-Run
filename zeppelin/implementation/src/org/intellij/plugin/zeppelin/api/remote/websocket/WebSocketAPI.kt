package org.intellij.plugin.zeppelin.api.remote.websocket

import com.intellij.openapi.diagnostic.Logger
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.*
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import org.intellij.plugin.zeppelin.models.ConnectionStatus
import org.intellij.plugin.zeppelin.models.SessionIsClosedException
import org.intellij.plugin.zeppelin.models.ZeppelinException
import org.intellij.plugin.zeppelin.utils.JsonParser
import java.net.URI

@Suppress("unused", "UNUSED_PARAMETER")
@WebSocket(maxTextMessageSize = 1024 * 1024)
class WebSocketAPI(address: String) {
    private val uri = "ws://$address/ws"
    private val logger: Logger = Logger.getInstance(WebSocketAPI::class.java)
    private val client: WebSocketClient = WebSocketClient()
    private val handlersMap: MutableMap<String, MessageHandler> = mutableMapOf()
    private val monitor = Object()
    private var defaultHandler: MessageHandler = object :
            MessageHandler {
        override fun handle(result: WsResponseMessage) {
            println("Default Handler is called. Operation: ${result.op}. Data: ${result.data}")
        }
    }
    var status = ConnectionStatus.DISCONNECTED
    private var session: Session? = null

    @OnWebSocketClose
    fun onClose(statusCode: Int, reason: String) {
        synchronized(monitor) {
            status = ConnectionStatus.DISCONNECTED
        }
    }

    @OnWebSocketError
    fun onError(throwable: Throwable) {
        synchronized(monitor) {
            status = ConnectionStatus.FAILED
            monitor.notifyAll()
        }
    }

    @OnWebSocketConnect
    fun onConnect(session: Session) {
        synchronized(monitor) {
            this.session = session
            status = ConnectionStatus.CONNECTED
            monitor.notifyAll()
        }
    }

    @OnWebSocketMessage
    fun onMessage(msg: String) {
        if (logger.isTraceEnabled) logger.trace("The message is handled $msg.")
        val response = JsonParser.fromStringObject(msg, WsResponseMessage::class.java)

        val operationCode = response.op
        handlersMap.getOrDefault(operationCode, defaultHandler).handle(response)
    }

    fun connect(): ConnectionStatus {
        val echoUri = URI(uri)
        client.start()
        val request = ClientUpgradeRequest()
        client.connect(this, echoUri, request)
        while (status == ConnectionStatus.DISCONNECTED) {
            waitMonitor()
        }
        return status
    }

    private fun waitMonitor() {
        synchronized(monitor) { monitor.wait() }
    }

    fun close() {
        client.stop()
    }

    fun doRequestSync(requestMessage: WsRequestMessage, responseCode: String): Any {
        if (status != ConnectionStatus.CONNECTED) throw SessionIsClosedException()
        var gotResponse = false
        var wsResponse: WsResponseMessage? = null

        val handleResult = object : MessageHandler {
            override fun handle(result: WsResponseMessage) {
                synchronized(monitor) {
                    wsResponse = result
                    gotResponse = true
                    monitor.notifyAll()
                }
            }
        }

        registerHandler(responseCode, handleResult)
        doRequestWithoutWaitingResult(requestMessage)

        synchronized(monitor) {
            while (!gotResponse && status == ConnectionStatus.CONNECTED) {
                monitor.wait()
            }
        }
        return wsResponse?.data ?: throw ZeppelinException()
    }

    fun doRequestWithoutWaitingResult(requestMessage: WsRequestMessage) {
        if (status != ConnectionStatus.CONNECTED) throw SessionIsClosedException()
        val msg: String = JsonParser.toJson(requestMessage)
        session?.remote?.sendString(msg)
    }

    fun registerHandler(op: String, handler: MessageHandler) {
        handlersMap.putAll(mapOf(op to handler))
    }

    fun doRequestAsync(requestMessage: WsRequestMessage) {
        doRequestWithoutWaitingResult(requestMessage)
    }
}