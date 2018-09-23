package org.intellij.plugin.zeppelin.api.remote

import org.intellij.plugin.zeppelin.api.remote.websocket.WebSocketAPI
import org.intellij.plugin.zeppelin.api.remote.websocket.ZeppelinWebSocketAPI
import org.intellij.plugin.zeppelin.api.remote.rest.RestAPI
import org.intellij.plugin.zeppelin.api.remote.rest.ZeppelinRestApi
import org.intellij.plugin.zeppelin.idea.settings.plugin.ZeppelinSettings
import org.intellij.plugin.zeppelin.service.execution.ExecutionHandlerFactory
import org.intellij.plugin.zeppelin.service.execution.ZeppelinExecutionManager

class ZeppelinIntegration(settings: ZeppelinSettings, executionHandlerFactory: ExecutionHandlerFactory) {
    private val restAPI = RestAPI(settings.host, settings.port)
    private val webSocketAPI: WebSocketAPI = WebSocketAPI(settings.fullUrl)

    private val zeppelinRestAPI = ZeppelinRestApi(restAPI)
    private val zeppelinWebSocketAPI = ZeppelinWebSocketAPI(webSocketAPI)

    val api = ZeppelinApi(zeppelinWebSocketAPI, zeppelinRestAPI, settings.user, settings.fullUrl)
    val executionManager: ZeppelinExecutionManager = ZeppelinExecutionManager(api, executionHandlerFactory)

    fun connect() = api.connect()

    fun isConnected(): Boolean = api.isConnected()
    fun close() = api.close()
}