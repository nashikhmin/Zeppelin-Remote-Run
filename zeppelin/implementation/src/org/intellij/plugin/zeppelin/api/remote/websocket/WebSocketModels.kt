package org.intellij.plugin.zeppelin.api.remote.websocket

import org.intellij.plugin.zeppelin.models.Credentials

data class RunParagraphData(val id: String,
                            val paragraph: String,
                            val title: String,
                            val settings: Map<String, String> = emptyMap(),
                            val params: Map<String, String> = emptyMap(),
                            val config: Map<String, String> = emptyMap())

/**
 * The zeppelin response web socket codes
 */
enum class ResponseCode {
    PARAGRAPH_UPDATE_OUTPUT,
    PARAGRAPH_APPEND_OUTPUT,
    PARAGRAPH,
    PARAGRAPH_ADDED,
    NOTE,
    PROGRESS,
    SAVE_NOTE_FORMS,
    NOTES_INFO,
    INTERPRETER_BINDINGS,
    PARAS_INFO
}

data class WsRequestMessage(val op: String, val data: Any, val ticket: String, val roles: String,
                            val principal: String) {
    companion object {
        fun create(op: String, data: Any, credentials: Credentials): WsRequestMessage {
            return WsRequestMessage(op, data, credentials.ticket,
                    credentials.roles, credentials.principal)
        }
    }
}

data class WsResponseMessage(val op: String, val data: Any)

interface MessageHandler {
    fun handle(result: WsResponseMessage)
}

/**
 * The response web socket codes
 */
enum class RequestOperations {
    GET_NOTE, RUN_PARAGRAPH, GET_INTERPRETER_BINDINGS, SAVE_INTERPRETER_BINDINGS
}

