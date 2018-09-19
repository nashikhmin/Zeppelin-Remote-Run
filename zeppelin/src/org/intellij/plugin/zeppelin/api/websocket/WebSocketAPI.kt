package org.intellij.plugin.zeppelin.api.websocket

import com.beust.klaxon.Klaxon
import com.intellij.openapi.diagnostic.Logger
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.*
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import org.intellij.plugin.zeppelin.models.ConnectionStatus
import org.intellij.plugin.zeppelin.models.ParseException
import org.intellij.plugin.zeppelin.models.SessionIsClosedException
import org.intellij.plugin.zeppelin.models.ZeppelinException
import java.net.URI

@WebSocket(maxTextMessageSize = 1024 * 1024)
class WebSocketAPI(private val uri: String) {
    private val LOG: Logger = Logger.getInstance(WebSocketAPI::class.java)
    private val client: WebSocketClient = WebSocketClient()
    private val handlersMap: MutableMap<String, MessageHandler> = mutableMapOf()
    private val monitor = Object()
    private var defaultHandler: MessageHandler = object : MessageHandler {
        override fun handle(result: ResponseMessage) {
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
        val response = Klaxon().parse<ResponseMessage>(msg) ?: throw ParseException(msg,
                ResponseMessage::class.toString())
        if (LOG.isTraceEnabled) LOG.trace("The message is handled $response.")
        val operationCode = response.op
        handlersMap.getOrDefault(operationCode, defaultHandler).handle(response)
    }

    fun connect(): ConnectionStatus {
        val echoUri: URI = URI(uri)
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

    fun doRequestSync(requestMessage: RequestMessage, responseCode: String): String {
        if (status != ConnectionStatus.CONNECTED) throw SessionIsClosedException()
        var gotResponse = false
        var response: ResponseMessage? = null

        val handleResult = object : MessageHandler {
            override fun handle(result: ResponseMessage) {
                synchronized(monitor) {
                    response = result
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
        return response?.data ?: throw ZeppelinException()
    }

    fun doRequestWithoutWaitingResult(requestMessage: RequestMessage) {
        if (status != ConnectionStatus.CONNECTED) throw SessionIsClosedException()
        val msg: String = Klaxon().toJsonString(requestMessage)
        session?.remote?.sendString(msg)
    }

    fun registerHandler(op: String, handler: MessageHandler) {
        handlersMap.putAll(mapOf(op to handler))
    }

    fun doRequestAsync(requestMessage: RequestMessage) {
        doRequestWithoutWaitingResult(requestMessage)
    }
}