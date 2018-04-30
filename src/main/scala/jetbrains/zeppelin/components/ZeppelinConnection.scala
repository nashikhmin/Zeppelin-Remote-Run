package jetbrains.zeppelin.components

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import jetbrains.zeppelin.service.ZeppelinService
import jetbrains.zeppelin.toolwindow.ZeppelinConsole

class ZeppelinConnection(val project: Project) extends ProjectComponent {
  val notebookName = s"RemoteNotebooks/${project.getName}"
  var username: String = ZeppelinConnection.DefaultZeppelinUser
  var password: String = ZeppelinConnection.DefaultZeppelinPassword
  var uri: String = ZeppelinConnection.DefaultZeppelinHost
  var port: Int = ZeppelinConnection.DefaultZeppelinPort
  private var zeppelinService: Option[ZeppelinService] = None

  private var outputConsole: Option[ZeppelinConsole] = None

  override def initComponent(): Unit = {
    super.initComponent()
  }

  def setUsername(value: String): Unit = {
    username = value
  }

  def setPassword(value: String): Unit = {
    password = value
  }

  def setUri(value: String): Unit = {
    uri = value
  }

  def setPort(value: Int): Unit = {
    port = value
  }

  def getHostURL: String = s"$uri:$port"

  def service: ZeppelinService = zeppelinService.getOrElse(resetApi())

  def printMessage(msg: String): Unit = console.print(msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT)

  def console: ZeppelinConsole = outputConsole.getOrElse(setConsole())

  def resetApi(): ZeppelinService = {
    zeppelinService.foreach(_.close())
    zeppelinService = Some(new ZeppelinService(uri, port, notebookName))
    if (username.nonEmpty || password.nonEmpty) zeppelinService.get.connect(username, password)
    zeppelinService.get
  }

  def setConsole(): ZeppelinConsole = {
    outputConsole = Some(new ZeppelinConsole(project))
    outputConsole.get
  }

  def printError(msg: String): Unit = console.print(msg + "\n", ConsoleViewContentType.ERROR_OUTPUT)
}

object ZeppelinConnection {
  val DefaultZeppelinHost = "localhost"
  val DefaultZeppelinPort = 8080
  val DefaultZeppelinUser = "admin"
  val DefaultZeppelinPassword = "password1"

  def connectionFor(project: Project): ZeppelinConnection = project.getComponent(classOf[ZeppelinConnection])
}
