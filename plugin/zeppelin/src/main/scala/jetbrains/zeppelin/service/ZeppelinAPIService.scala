package jetbrains.zeppelin.service

import jetbrains.zeppelin.api.rest.{RestApiException, ZeppelinRestApi}
import jetbrains.zeppelin.api.websocket.{OutputHandler, ZeppelinWebSocketAPI}
import jetbrains.zeppelin.models._
import jetbrains.zeppelin.utils.{ThreadRun, ZeppelinLogger}

/**
  * Class which implements the logic of communication with different API
  *
  * @param zeppelinWebSocketAPI - service for communication with Zeppelin by WebSockets
  * @param zeppelinRestApi      - service for communication with Zeppelin by REST API
  */
class ZeppelinAPIService private(val zeppelinWebSocketAPI: ZeppelinWebSocketAPI,
                                 val zeppelinRestApi: ZeppelinRestApi, val uri: String, val user: Option[User]) {
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
    * Get default interpreter for a notebook
    *
    * @param noteId - id of the notebook
    * @return default interpreter
    */
  def defaultInterpreter(noteId: String): Option[Interpreter] = {
    val defaultInterpreterId = zeppelinWebSocketAPI.getBindingInterpreters(noteId, credentials).headOption
      .getOrElse(throw new Exception)
      .name
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
    * Get from Zeppelin the notebook by the name. If the notebook does not exist the notebook will be created
    *
    * Additionally, the method check the count of paragraphs and if it is more than a predefined value, delete all paragraphs
    *
    * @param notebookName - the name of the notebook
    * @return notebook
    */
  def getOrCreateNotebook(notebookName: String): Notebook = {
    val note = zeppelinRestApi.getNotebooks().find(_.name == notebookName)
      .getOrElse(zeppelinRestApi.createNotebook(NewNotebook(notebookName)))

    var fullInfoNote = zeppelinWebSocketAPI.getNote(note.id, credentials)
    if (fullInfoNote.paragraphs.length > MAXIMUM_COUNT_OF_PARAGRAPHS_PER_NOTE) {
      deleteAllParagraphs(fullInfoNote.id)
      fullInfoNote = zeppelinWebSocketAPI.getNote(note.id, credentials)
    }
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
    * @param code         - the code, which must be executed
    * @param handler      - a handler, that must handle outputs and status
    * @param notebookName - a name of the notebook which is the place for executing the code
    */
  def runCode(code: String, handler: OutputHandler, notebookName: String): Unit = {
    val notebook = getOrCreateNotebook(notebookName)
    val paragraphId = zeppelinRestApi.createParagraph(notebook.id, code).id
    val notebookWS = zeppelinWebSocketAPI.getNote(notebook.id, credentials)
    val paragraph = notebookWS.paragraphs.find(_.id == paragraphId).get
    zeppelinWebSocketAPI.runParagraph(paragraph, handler, credentials)
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
}

object ZeppelinAPIService {
  def apply(address: String, port: Int, user: Option[User]): ZeppelinAPIService = {
    new ZeppelinAPIService(ZeppelinWebSocketAPI(address, port), ZeppelinRestApi(address, port),
      s"$address:$port", user)
  }
}