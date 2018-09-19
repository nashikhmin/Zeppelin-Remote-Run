package org.intellij.plugin.zeppelin.api.websocket

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

data class RequestMessage(val op: String, val data: String, val credentials: Credentials)

data class ResponseMessage(val op: String, val data: String)

interface MessageHandler {
    fun handle(result: ResponseMessage)
}

/**
 * The response web socket codes
 */
enum class RequestOperations {
    GET_NOTE, RUN_PARAGRAPH, GET_INTERPRETER_BINDINGS, SAVE_INTERPRETER_BINDINGS
}

