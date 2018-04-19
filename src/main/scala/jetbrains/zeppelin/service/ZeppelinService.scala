package jetbrains.zeppelin.service

import jetbrains.zeppelin.api._
import jetbrains.zeppelin.api.rest.ZeppelinRestApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, ZeppelinWebSocketAPI}

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
    zeppelinRestApi.updateInterpreterSettings(interpreter)
  }

  def interpreter: Interpreter = zeppelinRestApi.getInterpreters.head


  def close(): Unit = {
    zeppelinWebSocketAPI.close()
  }
}
