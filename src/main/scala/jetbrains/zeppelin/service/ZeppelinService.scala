package jetbrains.zeppelin.service

import jetbrains.zeppelin.api._
import jetbrains.zeppelin.api.rest.{RestApiException, ZeppelinRestApi}
import jetbrains.zeppelin.api.websocket.{OutputHandler, ZeppelinWebSocketAPI}
import jetbrains.zeppelin.utils.{ThreadRun, ZeppelinLogger}

/**
  * Main component, which is responsible for communication with Zeppelin application
  *
  * @param zeppelinWebSocketAPI - service for communication with Zeppelin by WebSockets
  * @param zeppelinRestApi      - service for communication with Zeppelin by REST API
  */
class ZeppelinService private(val zeppelinWebSocketAPI: ZeppelinWebSocketAPI,
                              val zeppelinRestApi: ZeppelinRestApi, val uri: String) {
  private var credentials: Credentials = Credentials("", "", "")
  private var connectionStatus = ConnectionStatus.DISCONNECTED

  /**
    * Connection to the Zeppelin server
    *
    * @param login    - the login of the user
    * @param password - the password of the user
    * @throws ZeppelinConnectionException if the service is unavailable
    * @throws ZeppelinLoginException      if the login/password is wrong
    */
  def connect(login: String, password: String): Unit = {
    connectionStatus = zeppelinWebSocketAPI.connect()
    connectionStatus match {
      case ConnectionStatus.FAILED_CONNECTION => throw new ZeppelinConnectionException(uri)
      case _ => Unit
    }
    try {
      credentials = zeppelinRestApi.login(login, password)
    }
    catch {
      case _: RestApiException => {
        connectionStatus = ConnectionStatus.FAILED_LOGIN
        throw new ZeppelinLoginException()
      }
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
    checkServiceAvailability()

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
    checkServiceAvailability()

    val note = zeppelinRestApi.getNotebooks().find(_.name == notebookName)
    note.getOrElse(zeppelinRestApi.createNotebook(NewNotebook(notebookName)))
  }

  /**
    * Update Jar file in the Zeppelin interpreter
    *
    * @param jarPath - the path to the jar file
    */
  def updateJar(jarPath: String): Unit = {
    checkServiceAvailability()

    var interpreter = zeppelinRestApi.getInterpreters.head
    val dependency = interpreter.dependencies.find(it => it.groupArtifactVersion == jarPath)
    if (dependency.isEmpty) {
      interpreter = interpreter.copy(dependencies = Dependency(jarPath) :: interpreter.dependencies)
    }
    ZeppelinLogger.printMessage(s"Upload jar to the Zeppelin, path: $jarPath")
    updateInterpreterSetting(interpreter)
  }

  /**
    * Update the settings of the interpreter
    *
    * @param  interpreter - settings of the new interpreter
    */
  def updateInterpreterSetting(interpreter: Interpreter): Unit = {
    checkServiceAvailability()

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
    * Check  plugin connection to the server
    *
    * @return connected or not
    */
  def isConnected: Boolean = {
    connectionStatus == ConnectionStatus.CONNECTED
  }

  /**
    * Get the default Scala Zeppelin interpreter
    *
    * @return interpreter
    */
  def interpreter: Interpreter = {
    checkServiceAvailability()

    val interpreter = zeppelinRestApi.getInterpreters.head
    if (interpreter.status == InterpreterStatus.ERROR) {
      throw new InterpreterException(interpreter)
    }
    interpreter
  }

  /**
    * Close the Zeppelin connection if it is opened
    */
  def close(): Unit = {
    zeppelinWebSocketAPI.close()
  }

  /**
    * Check is the Zeppelin service is available
    */
  private def checkServiceAvailability(): Unit = {
    if (connectionStatus != ConnectionStatus.CONNECTED) {
      throw new ServiceIsUnavailableException
    }
  }
}

object ZeppelinService {
  def apply(address: String, port: Int): ZeppelinService = {
    new ZeppelinService(ZeppelinWebSocketAPI(address, port), ZeppelinRestApi(address, port),
      s"$address:$port")
  }
}
