package org.intellij.plugin.zeppelin.api.websocket

import com.beust.klaxon.Klaxon
import com.intellij.openapi.diagnostic.Logger
import org.intellij.plugin.zeppelin.models.*

/**
 * The service for communication with the Zeppelin application by WebSockets
 *
 * @param webSocketAPI - web socket client
 */
class ZeppelinWebSocketAPI(private val webSocketAPI: WebSocketAPI) {
    private val logger = Logger.getInstance(this::class.java)

    init {
        listOf(ResponseCode.PARAGRAPH_ADDED,
                ResponseCode.PARAS_INFO,
                ResponseCode.SAVE_NOTE_FORMS,
                ResponseCode.NOTES_INFO)
                .forEach { code ->
                    webSocketAPI.registerHandler(code.toString(), object : MessageHandler {
                        override fun handle(result: ResponseMessage) {
                            if (logger.isTraceEnabled) logger.trace("Message ${result.op} is ignored")
                        }
                    })
                }
    }

    /**
     * Close the connection
     */
    fun close() {
        webSocketAPI.close()
        if (logger.isTraceEnabled) logger.trace("Connection is closed")
    }

    /**
     * Connect to the application
     */
    fun connect() {
        webSocketAPI.connect()
        if (logger.isTraceEnabled) logger.trace("Connection is opened")
    }

    fun connectionStatus() = webSocketAPI.status

    /**
     * Get list of interpreters that are available for the notebooks
     *
     * @param noteId      - id of the notebook
     * @param credentials - an user credentials
     * @return
     */
    fun getBindingInterpreters(noteId: String, credentials: Credentials): List<InterpreterBindings> {
        val data = mapOf("noteId" to noteId)
        val json = Klaxon().toJsonString(data)

        logger.trace("Start request 'Get bindings interpreters'. Data : $data, credentials: $credentials.")

        val opRequest = RequestOperations.GET_INTERPRETER_BINDINGS.toString()
        val opResponse = ResponseCode.INTERPRETER_BINDINGS.toString()

        val requestMessage = RequestMessage(opRequest, json, credentials)
        val responseJson = webSocketAPI.doRequestSync(requestMessage, opResponse)
        val interpreters = Klaxon().parse<List<InterpreterBindings>>(responseJson) ?: listOf()

        logger.trace("End of request 'Get bindings interpreters'. Response: $responseJson.")
        return interpreters
    }

    /**
     * Get model of the notebook
     *
     * @param noteId      - the id of the notebook
     * @param credentials - the credentials of the user
     * @return the model of the notebook
     */
    fun getNote(noteId: String, credentials: Credentials): Notebook? {
        val data = mapOf("id" to noteId)
        val json = Klaxon().toJsonString(data)
        if (logger.isTraceEnabled) logger.trace("Start request 'Get note'. Data : $data, credentials: $credentials.")
        val opRequest = RequestOperations.GET_NOTE.toString()
        val opResponse = ResponseCode.NOTE.toString()
        val requestMessage = RequestMessage(opRequest, json, credentials)
        val response = webSocketAPI.doRequestSync(requestMessage, opResponse)
        if (logger.isTraceEnabled) logger.trace("End of request 'Get note'. Response: $response.")
        return Klaxon().parse<Notebook>(response)
    }

    fun registerHandler(op: String, handler: MessageHandler) {
        webSocketAPI.registerHandler(op, handler)
    }

    /**
     * Run the paragraph in the Zeppelin application
     *
     * @param paragraph   - the paragraph, which must be run
     * @param credentials - the credentials of the user
     */
    fun runParagraph(paragraph: Paragraph, credentials: Credentials) {
        val data = RunParagraphData(paragraph.id, paragraph.text, paragraph.title)
        val json = Klaxon().toJsonString(data)

        val opRequest = RequestOperations.RUN_PARAGRAPH.toString()
        val requestMessage = RequestMessage(opRequest, json, credentials)

        logger.trace("Start request 'Run paragraph'. Data : $data, credentials: $credentials.")

        webSocketAPI.doRequestAsync(requestMessage)
    }

    /**
     * Save new interpreters bindings settings for the notebook
     *
     * @param noteId                  - id of the notebook
     * @param newInterpretersBindings - new interpreters bindings
     * @param credentials             - an user credentials
     */
    fun saveListOfBindingInterpreters(noteId: String,
                                      newInterpretersBindings: List<String>,
                                      credentials: Credentials) {
        val data = mapOf("noteId" to noteId, "selectedSettingIds" to newInterpretersBindings)
        val json = Klaxon().toJsonString(data)
        val opRequest = RequestOperations.SAVE_INTERPRETER_BINDINGS.toString()
        val requestMessage = RequestMessage(opRequest, json, credentials)

        logger.trace("Start request 'Save list of binding interpreters'. Data : $data, credentials: $credentials.")
        webSocketAPI.doRequestWithoutWaitingResult(requestMessage)
    }

    companion object {
        fun apply(address: String, port: Int): ZeppelinWebSocketAPI =
                ZeppelinWebSocketAPI(WebSocketAPI("ws://$address:$port/ws"))
    }
}

