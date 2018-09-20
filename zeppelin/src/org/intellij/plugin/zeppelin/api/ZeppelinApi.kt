package org.intellij.plugin.zeppelin.api

import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.experimental.delay
import org.intellij.plugin.zeppelin.api.websocket.MessageHandler
import org.intellij.plugin.zeppelin.api.websocket.ResponseCode
import org.intellij.plugin.zeppelin.api.websocket.ZeppelinWebSocketAPI
import org.intellij.plugin.zeppelin.model.RestApiException
import org.intellij.plugin.zeppelin.model.ZeppelinRestApi
import org.intellij.plugin.zeppelin.models.*
import org.intellij.plugin.zeppelin.service.InterpreterException
import org.intellij.plugin.zeppelin.service.InterpreterNotFoundException
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
 * Class which implements the logic of communication with Zeppelin API
 *
 * @param zeppelinWebSocketAPI - service for communication with Zeppelin by WebSockets
 * @param zeppelinRestApi      - service for communication with Zeppelin by REST API
 * @param user - an user if not anonymous
 */
class ZeppelinApi(private val zeppelinWebSocketAPI: ZeppelinWebSocketAPI,
                  private val zeppelinRestApi: ZeppelinRestApi,
                  val user: User?,
                  private val uri: String) {

    private val LOG: Logger = Logger.getInstance(this::class.java)
    private var credentials: Credentials = Credentials("anonymous", "anonymous", "")

    /**
     * Get a list of the available interpreters
     *
     * @return the list of interpreters
     */
    fun allInterpreters(): List<Interpreter> {
        return zeppelinRestApi.getInterpreters()
    }

    /**
     * Get a list of the available notebooks
     *
     * @return the list of notebooks
     */
    fun allNotebooks(): List<Notebook> {
        return zeppelinRestApi.getNotebooks()
    }

    /**
     * Close the Zeppelin connection if it is opened
     */
    fun close() {
        zeppelinWebSocketAPI.close()
        LOG.info("Zeppelin connection is closed")
    }

    /**
     * Connection to the Zeppelin server
     *
     * @throws ZeppelinConnectionException if the service is unavailable
     * @throws ZeppelinLoginException      if the name/password is wrong
     */
    fun connect() {
        zeppelinWebSocketAPI.connect()
        when (zeppelinWebSocketAPI.connectionStatus()) {
            ConnectionStatus.FAILED -> throw ZeppelinConnectionException(uri)
            ConnectionStatus.CONNECTED -> {
            }
            else -> throw Exception("Unhandled Status")
        }

        try {
            user?.let { credentials = zeppelinRestApi.login(it) }
        } catch (_: RestApiException) {
            throw ZeppelinLoginException()
        }
        ZeppelinLogger.printMessage("Connected to the Zeppelin")
    }

    /**
     * Create a notebook
     *
     * @param notebookName - a name of a notebook
     * @return a created notebook
     */
    fun createNotebook(notebookName: String): Notebook = zeppelinRestApi.createNotebook(NewNotebook(notebookName))

    /**
     * Create a paragraph in Zeppelin in the end of the notebook
     *
     * @param notebookId - an id of a notebook
     * @param text       - a text in paragraph
     * @return a model of a created paragraph
     */
    fun createParagraph(notebookId: String, text: String): Paragraph = zeppelinRestApi.createParagraph(notebookId, text)

    /**
     * Get default interpreter for a notebook
     *
     * @param noteId - id of the notebook
     * @return default interpreter
     */
    fun defaultInterpreter(noteId: String): Interpreter? {
        val defaultInterpreterId: String = zeppelinWebSocketAPI.getBindingInterpreters(noteId,
                credentials).firstOrNull()?.id
                ?: throw  NotebookNotFoundException(noteId)
        return this.allInterpreters().find { it.id == defaultInterpreterId }
    }

    /**
     * Delete all notebooks by prefix of a name
     *
     * @param prefix - a name prefix
     */
    fun deleteAllNotebooksByPrefix(prefix: String) {
        val notebooks: List<Notebook> = zeppelinRestApi.getNotebooks().filter { it.name.startsWith(prefix) }
        notebooks.forEach { note -> zeppelinRestApi.deleteNotebook(note.id) }
    }

    /**
     * Delete all paragraphs in a notebook
     *
     * @param noteId - an id of a notebook
     */
    fun deleteAllParagraphs(noteId: String) {
        val paragraphs: List<Paragraph> = zeppelinWebSocketAPI.getNote(noteId, credentials)?.paragraphs
                ?: throw NotebookNotFoundException(noteId)
        paragraphs.forEach { it ->
            zeppelinRestApi.deleteParagraph(noteId, it.id)
        }
    }

    /**
     * Delete a notebook
     *
     * @param notebook - a notebook model
     */
    fun deleteNotebook(notebook: Notebook) {
        zeppelinRestApi.deleteNotebook(notebook.id)
    }

    /**
     * Get a model of a notebook by id
     *
     * @param notebookId - an id of a notebook
     * @return an option with notebook
     */
    fun getNotebookById(notebookId: String): Notebook? {
        return zeppelinWebSocketAPI.getNote(notebookId, credentials)
    }

    /**
     * Get from Zeppelin the notebook by the name. If the notebook does not exist the notebook will be created
     *
     * Additionally, the method check the count of paragraphs and if it is more than a predefined value, delete all paragraphs
     *
     * @param notebookName - the name of the notebook
     * @return notebook
     */
    fun getOrCreateNotebook(notebookName: String): Notebook {
        val note = zeppelinRestApi.getNotebooks().firstOrNull { it.name == notebookName }
                ?: zeppelinRestApi.createNotebook(NewNotebook(notebookName))
        return zeppelinWebSocketAPI.getNote(note.id, credentials) ?: throw NotebookNotFoundException(notebookName)
    }

    /**
     * Get an interpreter by ID
     *
     * @param id                     - id of an interpreter
     * @param ignoreInterpreterError - ignore interpreters errors
     * @return interpreter
     */
    fun interpreterById(id: String, ignoreInterpreterError: Boolean = false): Interpreter? {
        val interpreter: Interpreter = allInterpreters().find { it.id == id } ?: return null
        if (interpreter.status == InterpreterStatus.ERROR && !ignoreInterpreterError) {
            InterpreterException(interpreter)
        }
        return interpreter
    }

    /**
     * Check  plugin connection to the server
     *
     * @return connected or not
     */
    fun isConnected(): Boolean {
        return zeppelinWebSocketAPI.connectionStatus() == ConnectionStatus.CONNECTED &&
                (user == null || zeppelinRestApi.loginStatus == LoginStatus.LOGGED)
    }

    fun registerHandler(responseCode: ResponseCode, handler: MessageHandler) {
        zeppelinWebSocketAPI.registerHandler(responseCode.toString(), handler)
    }

    /**
     * Restart an interpreter
     *
     * @param interpreterId - an interpreter
     * @param noteId        - an id of the notebook, where interpreter will be restarted
     */
    fun restartInterpreter(interpreterId: String, noteId: String) {
        val interpreter: Interpreter = interpreterById(interpreterId) ?: throw InterpreterNotFoundException(
                interpreterId)
        zeppelinRestApi.restartInterpreter(interpreter, noteId)
    }

    /**
     * Run the code on the zeppelin application
     *
     * @param executeContext - a context of execution
     */
    fun runCode(executeContext: ExecuteContext) {
        val paragraph: Paragraph? = getParagraph(executeContext.noteId, executeContext.paragraphId)
        paragraph?.let { zeppelinWebSocketAPI.runParagraph(paragraph, credentials) }
                ?: throw ParagraphNotFoundException(executeContext)
    }

    /**
     * Set a default interpreter for the notebook
     *
     * @param noteId        - id of the notebook
     * @param interpreterId - id of the interpreter
     */
    fun setDefaultInterpreter(noteId: String, interpreterId: String) {
        val bindingInterpreters: List<String> = zeppelinWebSocketAPI.getBindingInterpreters(noteId,
                credentials).map { it.id }
        val newDefaultInterpreter: String = bindingInterpreters.find { it == interpreterId }
                ?: throw InterpreterNotFoundException(interpreterId)
        val newBindingInterpreters: List<String> = listOf(
                newDefaultInterpreter) + bindingInterpreters.filter { it != newDefaultInterpreter }
        zeppelinWebSocketAPI.saveListOfBindingInterpreters(noteId, newBindingInterpreters, credentials)
    }

    /**
     * Update the settings of the interpreter
     *
     * @param  interpreter - settings of the new interpreter
     */
    suspend fun updateInterpreterSetting(interpreter: Interpreter) {

        zeppelinRestApi.updateInterpreterSettings(interpreter)
        ZeppelinLogger.printMessage("Start updating the ${interpreter.name} interpreter settings...")
        var count = 0
        val messageTime: Int = 2 * 1000

        while (interpreterById(interpreter.id)?.status == InterpreterStatus.DOWNLOADING_DEPENDENCIES) {
            if (count % messageTime == 0) {
                ZeppelinLogger.printMessage("Updating the ${interpreter.name} interpreter settings...")
            }
            val waitTime = 200
            count += waitTime
            delay(waitTime.toLong())
        }


        ZeppelinLogger.printMessage("The settings of the ${interpreter.name} interpreter have been updated")
    }

    private fun getParagraph(notebookId: String, paragraphId: String): Paragraph? {
        val notebookWS: Notebook = zeppelinWebSocketAPI.getNote(notebookId, credentials) ?: return null
        return notebookWS.paragraphs.find { it.id == paragraphId }
    }
}