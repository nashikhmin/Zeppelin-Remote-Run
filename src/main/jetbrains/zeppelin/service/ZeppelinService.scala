package jetbrains.zeppelin.service

import jetbrains.zeppelin.api.rest.ZeppelinRestApi
import jetbrains.zeppelin.api.websocket.{OutputHandler, ZeppelinWebSocketAPI}
import jetbrains.zeppelin.api.{Credentials, NewNotebook, Notebook}

class ZeppelinService(val zeppelinWebSocketAPI: ZeppelinWebSocketAPI,
                      val zeppelinRestApi: ZeppelinRestApi,
                      val notebookName: String) {
  private lazy val notebook: Notebook = getOrCreateNotebook(notebookName)
  private var credentials: Credentials = Credentials("", "", "")

  def this(address: String, port: Int, notebookName: String) {
    this(new ZeppelinWebSocketAPI(address, port), new ZeppelinRestApi(address, port), notebookName)
  }

  def login(login: String, password: String): Unit = {
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
}
