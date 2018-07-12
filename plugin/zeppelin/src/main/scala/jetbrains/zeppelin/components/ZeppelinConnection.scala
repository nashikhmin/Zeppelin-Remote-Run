package jetbrains.zeppelin.components

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import jetbrains.zeppelin.api.User
import jetbrains.zeppelin.service.ZeppelinActionService
import jetbrains.zeppelin.settings.{RemoteRunApplicationSettings, ZeppelinSettings}
import jetbrains.zeppelin.toolwindow.{InterpretersView, ZeppelinConsole}
import jetbrains.zeppelin.utils.ZeppelinLogger

/**
  * Class which handle an actual [[ZeppelinActionService]]
  *
  * @param project - an owner project
  */
class ZeppelinConnection(val project: Project) extends ProjectComponent {
  val interpretersView: InterpretersView = new InterpretersView
  private var zeppelinActionService: Option[ZeppelinActionService] = None

  override def initComponent(): Unit = {
    super.initComponent()
    ZeppelinLogger.initOutput(new ZeppelinConsole(project))
  }

  /**
    * Update stored Zeppelin settings
    *
    * @param newSettings - new Zeppelin settings
    */
  def updateSettings(newSettings: ZeppelinSettings): Unit = {
    val settings = RemoteRunApplicationSettings.getInstance(project)
    settings.setZeppelinSettings(newSettings)
    val state = settings.getState
    settings.loadState(state)
    resetApi()
  }

  /**
    * Full restart of all connections
    *
    * @return new action service
    */
  def resetApi(): ZeppelinActionService = {
    zeppelinActionService.foreach(_.destroy())

    val zeppelinSettings = getZeppelinSettings
    val user = if (zeppelinSettings.isAnonymous) None else Some(User(zeppelinSettings.login, zeppelinSettings.password))
    zeppelinActionService = Some(ZeppelinActionService(zeppelinSettings.address, zeppelinSettings.port, user))
    zeppelinActionService.get
  }

  /**
    * Get saved zeppelin settings
    *
    * @return settings
    */
  def getZeppelinSettings: ZeppelinSettings = {
    RemoteRunApplicationSettings.getInstance(project).getZeppelinSettings
  }

  /**
    * Update list of  interpreters for the notebook
    *
    * @param notebookName - a name of the notebook
    */
  def updateInterpreterList(notebookName: String): Unit = {
    val interpretersNames = service.interpreterList(notebookName).map(_.name)
    interpretersView.updateInterpretersList(interpretersNames)
  }

  def service: ZeppelinActionService = zeppelinActionService.getOrElse(resetApi())
}

object ZeppelinConnection {
  def connectionFor(project: Project): ZeppelinConnection = project.getComponent(classOf[ZeppelinConnection])
}