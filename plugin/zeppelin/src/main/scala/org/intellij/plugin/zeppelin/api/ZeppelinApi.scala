package org.intellij.plugin.zeppelin.api

import com.intellij.openapi.diagnostic.Logger
import org.intellij.plugin.zeppelin.api.rest.{RestApiException, ZeppelinRestApi}
import org.intellij.plugin.zeppelin.api.websocket.{MessageHandler, ResponseCode, ZeppelinWebSocketAPI}
import org.intellij.plugin.zeppelin.models._
import org.intellij.plugin.zeppelin.service.{InterpreterException, InterpreterNotFoundException}
import org.intellij.plugin.zeppelin.utils.{ThreadRun, ZeppelinLogger}

import scala.util.Try

/**
  * Class which implements the logic of communication with Zeppelin API
  *
  * @param zeppelinWebSocketAPI - service for communication with Zeppelin by WebSockets
  * @param zeppelinRestApi      - service for communication with Zeppelin by REST API
  */
class ZeppelinApi private(val zeppelinWebSocketAPI: ZeppelinWebSocketAPI,
                          val zeppelinRestApi: ZeppelinRestApi,
                          val uri: String,
                          val user: Option[User]) {
  private val LOG = Logger.getInstance(getClass)
  private val MAXIMUM_COUNT_OF_PARAGRAPHS_PER_NOTE = 15
  private var credentials: Credentials = Credentials("anonymous", "anonymous", "")

  /**
    * Get a list of the available interpreters
    *
    * @return the list of interpreters
    */
  def allInterpreters: List[Interpreter] = {
    zeppelinRestApi.getInterpreters
  }

  /**
    * Get a list of the available notebooks
    *
    * @return the list of notebooks
    */
  def allNotebooks: List[Notebook] = {
    zeppelinRestApi.getNotebooks()
  }

  /**
    * Close the Zeppelin connection if it is opened
    */
  def close(): Unit = {
    zeppelinWebSocketAPI.close()
    LOG.info("Zeppelin connection is closed")
  }

  /**
    * Connection to the Zeppelin server
    *
    * @param needAuth is needed authentication by login/password
    * @throws ZeppelinConnectionException if the service is unavailable
    * @throws ZeppelinLoginException      if the login/password is wrong
    */
  def connect(needAuth: Boolean = true): Unit = {
    zeppelinWebSocketAPI.connect()
    zeppelinWebSocketAPI.connectionStatus match {
      case ConnectionStatus.FAILED => throw ZeppelinConnectionException(uri)
      case ConnectionStatus.CONNECTED => Unit
      case _ => throw new Exception("Unhandled Status")
    }

    try {
      user.foreach(it => credentials = zeppelinRestApi.login(it.login, it.password))
    }
    catch {
      case _: RestApiException => throw ZeppelinLoginException()
    }

    ZeppelinLogger.printMessage("Connected to the Zeppelin")
  }

  /**
    * Create a notebook
    *
    * @param notebookName - a name of a notebook
    * @return a created notebook
    */
  def createNotebook(notebookName: String): Notebook = {
    zeppelinRestApi.createNotebook(NewNotebook(notebookName))
  }

  /**
    * Create a paragraph in Zeppelin in the end of the notebook
    *
    * @param notebookId - an id of a notebook
    * @param text       - a text in paragraph
    * @return a model of a created paragraph
    */
  def createParagraph(notebookId: String, text: String): Paragraph = {
    zeppelinRestApi.createParagraph(noteId = notebookId, text)
  }

  /**
    * Get default interpreter for a notebook
    *
    * @param noteId - id of the notebook
    * @return default interpreter
    */
  def defaultInterpreter(noteId: String): Option[Interpreter] = {
    val defaultInterpreterId = zeppelinWebSocketAPI.getBindingInterpreters(noteId, credentials).headOption
      .getOrElse(throw new Exception)
      .id
    this.allInterpreters.find(_.id == defaultInterpreterId)
  }

  /**
    * Delete all notebooks by prefix of a name
    *
    * @param prefix - a name prefix
    */
  def deleteAllNotebooksByPrefix(prefix: String): Unit = {
    val notebooks = zeppelinRestApi.getNotebooks(prefix)
    notebooks.foreach(note => zeppelinRestApi.deleteNotebook(note.id))
  }

  /**
    * Delete all paragraphs in a notebook
    *
    * @param noteId - an id of a notebook
    */
  def deleteAllParagraphs(noteId: String): Unit = {
    val paragraphs = zeppelinWebSocketAPI.getNote(noteId, credentials).paragraphs
    paragraphs.foreach(it => zeppelinRestApi.deleteParagraph(noteId, it.id))
  }

  /**
    * Delete a notebook
    *
    * @param notebook - a notebook model
    */
  def deleteNotebook(notebook: Notebook): Unit = {
    zeppelinRestApi.deleteNotebook(notebook.id)
  }

  /**
    * Get a model of a notebook by id
    *
    * @param notebookId - an id of a notebook
    * @return an option with notebook
    */
  def getNotebookById(notebookId: String): Option[Notebook] = {
    Try(zeppelinWebSocketAPI.getNote(notebookId, credentials)).toOption
  }

  /**
    * Get from Zeppelin the notebook by the name. If the notebook does not exist the notebook will be created
    *
    * Additionally, the method check the count of paragraphs and if it is more than a predefined value, delete all paragraphs
    *
    * @param notebookName - the name of the notebook
    * @return notebook
    */
  def getOrCreateNotebook(notebookName: String): Notebook = {
    val note = zeppelinRestApi.getNotebooks().find(_.name == notebookName)
      .getOrElse(createNotebook(notebookName))

    var fullInfoNote = zeppelinWebSocketAPI.getNote(note.id, credentials)
    //TODO: REMOVE
//    if (fullInfoNote.paragraphs.length > MAXIMUM_COUNT_OF_PARAGRAPHS_PER_NOTE) {
//      deleteAllParagraphs(fullInfoNote.id)
//      fullInfoNote = zeppelinWebSocketAPI.getNote(note.id, credentials)
//    }
    fullInfoNote
  }

  /**
    * Get an interpreter by ID
    *
    * @param id                     - id of an interpreter
    * @param ignoreInterpreterError - ignore interpreters errors
    * @return interpreter
    */
  def interpreterById(id: String, ignoreInterpreterError: Boolean = false): Interpreter = {
    val interpreter = allInterpreters.find(_.id == id).getOrElse(throw new InterpreterNotFoundException(id))
    if (interpreter.status == InterpreterStatus.ERROR && !ignoreInterpreterError) {
      throw new InterpreterException(interpreter)
    }
    interpreter
  }

  /**
    * Check  plugin connection to the server
    *
    * @return connected or not
    */
  def isConnected: Boolean = {
    zeppelinWebSocketAPI.connectionStatus == ConnectionStatus.CONNECTED &&
      (user.isEmpty || zeppelinRestApi.loginStatus == LoginStatus.LOGGED)
  }

  def registerHandler(responseCode: ResponseCode.ResponseCode, handler: MessageHandler): Unit = {
    zeppelinWebSocketAPI.registerHandler(responseCode.toString, handler)
  }

  /**
    * Restart an interpreter
    *
    * @param interpreterId - an interpreter
    * @param noteId        - an id of the notebook, where interpreter will be restarted
    */
  def restartInterpreter(interpreterId: String, noteId: String): Unit = {
    val interpreter = interpreterById(interpreterId)
    zeppelinRestApi.restartInterpreter(interpreter, noteId)
  }

  /**
    * Run the code on the zeppelin application
    *
    * @param executeContext - a context of execution
    */
  def runCode(executeContext: ExecuteContext): Unit = {
    val maybeParagraph = getParagraph(executeContext.noteId, executeContext.paragraphId)
    if (maybeParagraph.isEmpty) throw ParagraphNotFoundException(executeContext)
    zeppelinWebSocketAPI.runParagraph(maybeParagraph.get, credentials)
  }

  /**
    * Set a default interpreter for the notebook
    *
    * @param noteId        - id of the notebook
    * @param interpreterId - id of the interpreter
    */
  def setDefaultInterpreter(noteId: String, interpreterId: String): Unit = {
    val bindingInterpreters = zeppelinWebSocketAPI.getBindingInterpreters(noteId, credentials).map(_.id)
    val newDefaultInterpreter = bindingInterpreters.find(_ == interpreterId)
      .getOrElse(throw new InterpreterNotFoundException(interpreterId))
    val newBindingInterpreters = newDefaultInterpreter +: bindingInterpreters.filter(_ != newDefaultInterpreter)
    zeppelinWebSocketAPI.saveListOfBindingInterpreters(noteId, newBindingInterpreters, credentials)
  }

  /**
    * Update the settings of the interpreter
    *
    * @param  interpreter - settings of the new interpreter
    */
  def updateInterpreterSetting(interpreter: Interpreter): Unit = {
    zeppelinRestApi.updateInterpreterSettings(interpreter)
    ZeppelinLogger.printMessage(s"Start updating the ${interpreter.name} interpreter settings...")
    ThreadRun.runWithTimeout {
      var count = 0
      val messageTime = 2 * 1000
      try {
        while (this.interpreterById(interpreter.id).status == InterpreterStatus.DOWNLOADING_DEPENDENCIES) {
          if (count % messageTime == 0) {
            ZeppelinLogger.printMessage(s"Updating the ${interpreter.name} interpreter settings...")
          }
          val waitTime = 200
          count += waitTime
          Thread.sleep(waitTime)
        }
      }
      catch {
        case e: InterpreterException => {
          ZeppelinLogger.printError("Error during updating interpreter settings." + e.getMessage)
        }
      }
    }
    ZeppelinLogger.printMessage(s"The settings of the ${interpreter.name} interpreter have been updated")
  }

  private def getParagraph(notebookId: String, paragraphId: String): Option[Paragraph] = {
    val notebookWS = zeppelinWebSocketAPI.getNote(notebookId, credentials)
    notebookWS.paragraphs.find(_.id == paragraphId)
  }
}

object ZeppelinApi {
  def apply(address: String, port: Int, user: Option[User]): ZeppelinApi = {
    new ZeppelinApi(ZeppelinWebSocketAPI(address, port), ZeppelinRestApi(address, port),
      s"$address:$port", user)
  }
}