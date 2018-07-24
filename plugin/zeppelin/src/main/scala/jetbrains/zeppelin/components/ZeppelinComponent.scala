package jetbrains.zeppelin.components

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import jetbrains.zeppelin.api.User
import jetbrains.zeppelin.service.ZeppelinActionService
import jetbrains.zeppelin.settings.{RemoteRunApplicationSettings, ZeppelinSettings}
import jetbrains.zeppelin.ui.toolwindow.{InterpretersView, ZeppelinConsole, ZeppelinToolWindowFactory}
import jetbrains.zeppelin.utils.ZeppelinLogger

/**
  * The main component that contains all used services
  *
  * @param project - an owner project
  */
class ZeppelinComponent(val project: Project) extends ProjectComponent {
  val interpretersView: InterpretersView = new InterpretersView
  private var zeppelinActionService: Option[ZeppelinActionService] = None


  /**
    * Open the LogTab
    */
  def focusToLog(): Unit = {
    val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ZeppelinToolWindowFactory.ID)
    toolWindow.show(null)
    val content = toolWindow.getContentManager.getContent(0)
    toolWindow.getContentManager.setSelectedContent(content)
  }

  /**
    * Get saved zeppelin settings
    *
    * @return settings
    */
  def getZeppelinSettings: ZeppelinSettings = {
    RemoteRunApplicationSettings.getInstance(project).getZeppelinSettings
  }

  override def initComponent(): Unit = {
    super.initComponent()
    ZeppelinLogger.initOutput(new ZeppelinConsole(project))
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
    zeppelinActionService = Some(ZeppelinActionService(project, zeppelinSettings.address, zeppelinSettings.port, user))
    zeppelinActionService.get
  }

  /**
    * Get service for actions
    *
    * @return
    */
  def service: ZeppelinActionService = {
    zeppelinActionService.getOrElse(resetApi())
  }

  /**
    * Update list of  interpreters for the notebook
    */
  def updateInterpreterList(force: Boolean = false): Unit = {
    if (force || interpretersView.isShowing) {
      val interpretersNames = service.interpreterList().map(_.name)
      interpretersView.updateInterpretersList(interpretersNames)
    }
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
}

object ZeppelinComponent {
  def connectionFor(project: Project): ZeppelinComponent = project.getComponent(classOf[ZeppelinComponent])
}