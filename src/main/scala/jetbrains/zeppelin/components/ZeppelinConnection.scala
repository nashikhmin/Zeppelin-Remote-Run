package jetbrains.zeppelin.components

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import jetbrains.zeppelin.api.User
import jetbrains.zeppelin.service.ZeppelinActionService
import jetbrains.zeppelin.toolwindow.{InterpretersView, ZeppelinConsole}
import jetbrains.zeppelin.utils.ZeppelinLogger

/**
  * Class which handle an actual [[ZeppelinActionService]]
  *
  * @param project - an owner project
  */
class ZeppelinConnection(val project: Project) extends ProjectComponent {
  val interpretersView: InterpretersView = new InterpretersView
  var anonymousAccess: Boolean = ZeppelinConnection.DefaultZeppelinAnonymousAccess
  var username: String = ZeppelinConnection.DefaultZeppelinUser
  var password: String = ZeppelinConnection.DefaultZeppelinPassword
  var uri: String = ZeppelinConnection.DefaultZeppelinHost
  var port: Int = ZeppelinConnection.DefaultZeppelinPort
  private var zeppelinActionService: Option[ZeppelinActionService] = None

  override def initComponent(): Unit = {
    super.initComponent()
    ZeppelinLogger.initOutput(new ZeppelinConsole(project))
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

  def setAnonymousAccess(value: Boolean): Unit = {
    anonymousAccess = value
  }

  def getHostURL: String = s"$uri:$port"

  def service: ZeppelinActionService = zeppelinActionService.getOrElse(resetApi())

  def resetApi(): ZeppelinActionService = {
    zeppelinActionService.foreach(_.destroy())
    val user = if (anonymousAccess) None else Some(User(username, password))
    zeppelinActionService = Some(ZeppelinActionService(uri, port, user))
    zeppelinActionService.get
  }
}

object ZeppelinConnection {
  val DefaultZeppelinHost = "localhost"
  val DefaultZeppelinPort = 8080
  val DefaultZeppelinUser = "admin"
  val DefaultZeppelinPassword = "password1"
  val DefaultZeppelinAnonymousAccess = false

  def connectionFor(project: Project): ZeppelinConnection = project.getComponent(classOf[ZeppelinConnection])
}