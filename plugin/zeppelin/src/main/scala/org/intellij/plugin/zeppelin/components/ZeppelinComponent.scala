package org.intellij.plugin.zeppelin.components

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.idea.settings.interpreter.UpdateInterpreterHandler
import org.intellij.plugin.zeppelin.idea.settings.plugin.ZeppelinSettings
import org.intellij.plugin.zeppelin.idea.toolwindow.{InterpretersView, ZeppelinConsole, ZeppelinToolWindowFactory}
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.service.ZeppelinActionService
import org.intellij.plugin.zeppelin.settings.RemoteRunApplicationSettings
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
  * The main component that contains all used services
  *
  * @param project - an owner project
  */
class ZeppelinComponent(val project: Project) extends ProjectComponent {
  val interpretersView: InterpretersView = new InterpretersView(project)
  var sparkVersion: SparkVersion = SparkVersion.ZEPPELIN_DEFAULT_VERSION
  var defaultNotebook: String = ""
  private var actions: Option[ZeppelinActionService] = None

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
    ZeppelinLogger.printMessage(ZeppelinConstants.RESTART_CONNECTION)
    actions.foreach(_.destroy())

    val zeppelinSettings = getZeppelinSettings
    sparkVersion = SparkVersion(zeppelinSettings.sparkVersion)
    defaultNotebook = zeppelinSettings.defaultNotebookName
    val user = zeppelinSettings.user
    actions = Some(ZeppelinActionService(project, zeppelinSettings))
    actions.get
  }

  /**
    * Get service for actions
    *
    * @return
    */
  def service: ZeppelinActionService = {
    actions.getOrElse(resetApi())
  }

  /**
    * Update list of  interpreters for the notebook
    */
  def updateInterpreterList(force: Boolean = false): Unit = {
    if (force || interpretersView.isShowing) {
      val interpretersNames = service.interpreterList().map(_.name)
      interpretersView.updateInterpretersList(interpretersNames)
      UpdateInterpreterHandler.getAll.foreach(_.updateInterpreter(project))
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