package jetbrains.zeppelin.service

import jetbrains.zeppelin.api._
import jetbrains.zeppelin.api.rest.{RestApiException, ZeppelinRestApi}
import jetbrains.zeppelin.api.websocket.{OutputHandler, ZeppelinWebSocketAPI}
import jetbrains.zeppelin.utils.{ThreadRun, ZeppelinLogger}

/**
  * Class which implements the logic of communication with different API
  *
  * @param zeppelinWebSocketAPI - service for communication with Zeppelin by WebSockets
  * @param zeppelinRestApi      - service for communication with Zeppelin by REST API
  */
class ZeppelinAPIService private(val zeppelinWebSocketAPI: ZeppelinWebSocketAPI,
                                 val zeppelinRestApi: ZeppelinRestApi, val uri: String, val user: Option[User]) {
  private var credentials: Credentials = Credentials("anonymous", "anonymous", "")


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
    * Get from Zeppelin the notebook by the name. If the notebook does not exist the notebook will be created
    *
    * @param notebookName - the name of the notebook
    * @return notebook
    */
  def getOrCreateNotebook(notebookName: String): Notebook = {
    val note = zeppelinRestApi.getNotebooks().find(_.name == notebookName)
    note.getOrElse(zeppelinRestApi.createNotebook(NewNotebook(notebookName)))
  }


  /**
    * Update Jar file in the Zeppelin interpreter
    *
    * @param jarPath - the path to the jar file
    */
  def updateJar(jarPath: String): Unit = {
    var interpreter = zeppelinRestApi.getInterpreters.head
    val dependency = interpreter.dependencies.find(it => it.groupArtifactVersion == jarPath)
    if (dependency.isEmpty) {
      interpreter = interpreter.copy(dependencies = Dependency(jarPath) :: interpreter.dependencies)
    }
    ZeppelinLogger.printMessage(s"Upload the jar to the Zeppelin")
    updateInterpreterSetting(interpreter)
    ZeppelinLogger.printMessage(s"The jar has been uploaded")
  }

  /**
    * Update the settings of the interpreter
    *
    * @param  interpreter - settings of the new interpreter
    */
  def updateInterpreterSetting(interpreter: Interpreter): Unit = {
    zeppelinRestApi.updateInterpreterSettings(interpreter)
    ZeppelinLogger.printMessage(s"Update the ${interpreter.name} interpreter settings")
    ThreadRun.runWithTimeout {
      var count = 0
      val messageTime = 2 * 1000
      while (this.interpreter.status == InterpreterStatus.DOWNLOADING_DEPENDENCIES) {
        if (count % messageTime == 0) {
          ZeppelinLogger.printMessage(s"Updating the ${interpreter.name} interpreter settings...")
        }
        val waitTime = 200
        count += waitTime
        Thread.sleep(waitTime)
      }
    }
    ZeppelinLogger.printMessage(s"The settings of the ${interpreter.name} interpreter have been updated")
  }

  /**
    * Get the default Notebook interpreter
    *
    * @return interpreter
    */
  def interpreter: Interpreter = {
    val interpreter = allInterpreters.head
    if (interpreter.status == InterpreterStatus.ERROR) {
      throw new InterpreterException(interpreter)
    }
    interpreter
  }

  /**
    * Get default interpreter for a notebook
    *
    * @param noteId - id of the notebook
    * @return default interpreter
    */
  def defaultInterpreter(noteId: String): Interpreter = {
    val defaultInterpreterId = zeppelinWebSocketAPI.getBindingInterpreters(noteId, credentials).headOption
      .getOrElse(throw new Exception)
      .name
    this.allInterpreters.find(_.id == defaultInterpreterId).get
  }

  /**
    * Get a list of the available interpreters
    *
    * @return the list of interpreters
    */
  def allInterpreters: List[Interpreter] = {
    zeppelinRestApi.getInterpreters
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
    * Close the Zeppelin connection if it is opened
    */
  def close(): Unit = {
    zeppelinWebSocketAPI.close()
  }
}

object ZeppelinAPIService {
  def apply(address: String, port: Int, user: Option[User]): ZeppelinAPIService = {
    new ZeppelinAPIService(ZeppelinWebSocketAPI(address, port), ZeppelinRestApi(address, port),
      s"$address:$port", user)
  }
}
