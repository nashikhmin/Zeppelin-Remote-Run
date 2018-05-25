package jetbrains.zeppelin.service

import jetbrains.zeppelin.api._
import jetbrains.zeppelin.api.rest.ZeppelinRestApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, ZeppelinWebSocketAPI}
import jetbrains.zeppelin.utils.{ThreadRun, ZeppelinLogger}

class ZeppelinService(val zeppelinWebSocketAPI: ZeppelinWebSocketAPI,
                      val zeppelinRestApi: ZeppelinRestApi,
                      val notebookName: String) {
  private lazy val notebook: Notebook = getOrCreateNotebook(notebookName)
  private var credentials: Credentials = Credentials("", "", "")

  def this(address: String, port: Int, notebookName: String) {
    this(new ZeppelinWebSocketAPI(address, port), new ZeppelinRestApi(address, port), notebookName)
  }

  def connect(login: String, password: String): Unit = {
    zeppelinWebSocketAPI.connect()
    credentials = zeppelinRestApi.login(login, password)
  }

  def getOrCreateNotebook(notebookName: String): Notebook = {
    val note = zeppelinRestApi.getNotes().find(_.name == notebookName)
    note.getOrElse(zeppelinRestApi.createNotebook(NewNotebook(notebookName)))
  }

  def runCode(code: String, handler: OutputHandler): Unit = {
    val paragraphId = zeppelinRestApi.createParagraph(notebook.id, code).id
    val notebookWS = zeppelinWebSocketAPI.getNote(notebook.id, credentials)
    val paragraph = notebookWS.paragraphs.find(_.id == paragraphId).get
    zeppelinWebSocketAPI.runParagraph(paragraph, handler, credentials)
  }

  def updateJar(jarPath: String): Unit = {
    var interpreter = zeppelinRestApi.getInterpreters.head
    val dependency = interpreter.dependencies.find(it => it.groupArtifactVersion == jarPath)
    if (dependency.isEmpty) {
      interpreter = interpreter.copy(dependencies = Dependency(jarPath) :: interpreter.dependencies)
    }
    ZeppelinLogger.printMessage("Add dependencies...")
    updateInterpreterSetting(interpreter)
  }

  def updateInterpreterSetting(interpreter: Interpreter): Unit = {
    zeppelinRestApi.updateInterpreterSettings(interpreter)

    ThreadRun.runWithTimeout {
      var count = 0
      val messageTime = 2 * 1000
      while (this.interpreter.status == InterpreterStatus.DOWNLOADING_DEPENDENCIES) {
        if (count % messageTime == 0)
          ZeppelinLogger.printMessage("Wait load dependencies...")
        val waitTime = 200
        count += waitTime
        Thread.sleep(waitTime)
      }
    }
  }

  def interpreter: Interpreter = {
    val interpreter = zeppelinRestApi.getInterpreters.head
    if (interpreter.status == InterpreterStatus.ERROR) {
      throw new InterpreterException(interpreter)
    }
    interpreter
  }


  def close(): Unit = {
    zeppelinWebSocketAPI.close()
  }
}
