package org.intellij.plugin.zeppelin.components

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.extensionpoints.UpdateInterpreterHandler
import org.intellij.plugin.zeppelin.idea.settings.plugin.RemoteRunApplicationSettings
import org.intellij.plugin.zeppelin.idea.settings.plugin.ZeppelinSettings
import org.intellij.plugin.zeppelin.idea.toolwindow.InterpretersView
import org.intellij.plugin.zeppelin.idea.toolwindow.ZeppelinConsole
import org.intellij.plugin.zeppelin.idea.toolwindow.ZeppelinToolWindowFactory
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.models.ZeppelinException
import org.intellij.plugin.zeppelin.service.ZeppelinActionService
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
 * The main component that contains all used services
 *
 * @param project - an owner project
 */
class ZeppelinComponent(val project: Project) : ProjectComponent {
    val interpretersView: InterpretersView = InterpretersView(project)

    var sparkVersion: SparkVersion = SparkVersion.ZEPPELIN_DEFAULT_VERSION
    var defaultNotebook: String = ""
    private var actions: ZeppelinActionService? = null

    /**
     * Open the LogTab
     */
    fun focusToLog() {
        val toolWindow: ToolWindow = ToolWindowManager.getInstance(project).getToolWindow(ZeppelinToolWindowFactory.ID)
        toolWindow.show(null)
        val content: Content = toolWindow.contentManager.getContent(0) ?: throw ZeppelinException(
                "Log content is not found")
        toolWindow.contentManager.setSelectedContent(content)
    }

    /**
     * Get saved zeppelin settings
     *
     * @return settings
     */
    fun getZeppelinSettings(): ZeppelinSettings {
        return RemoteRunApplicationSettings.getInstance(project).getZeppelinSettings()
    }

    override fun initComponent() {
        super.initComponent()
        ZeppelinLogger.output = ZeppelinConsole(project)
    }

    /**
     * Full restart of all connections
     *
     * @return new action service
     */
    fun resetApi(): ZeppelinActionService {
        ZeppelinLogger.printMessage(ZeppelinConstants.RESTART_CONNECTION)
        actions?.destroy()
        val zeppelinSettings: ZeppelinSettings = getZeppelinSettings()
        sparkVersion = SparkVersion(zeppelinSettings.sparkVersion)
        defaultNotebook = zeppelinSettings.defaultNotebookName
        val zeppelinActionService = ZeppelinActionService(project, zeppelinSettings)
        actions = zeppelinActionService
        return zeppelinActionService
    }

    val service: ZeppelinActionService
        get() = actions ?: resetApi()

    /**
     * Update list of  interpreters for the notebook
     */
    fun updateInterpreterList(force: Boolean) {
        if (force || interpretersView.isShowing) {
            val interpretersNames = service.interpreterList().map { it.name }
            interpretersView.updateInterpretersList(interpretersNames)
            UpdateInterpreterHandler.getAll().forEach { it.updateInterpreter(project) }
        }
    }

    /**
     * Update stored Zeppelin settings
     *
     * @param newSettings - new Zeppelin settings
     */
    fun updateSettings(newSettings: ZeppelinSettings) {
        val settings = RemoteRunApplicationSettings.getInstance(project)
        settings.setZeppelinSettings(newSettings)
        val state = settings.myState
        settings.loadState(state)
        resetApi()
    }

    companion object {
        @JvmStatic
        fun connectionFor(project: Project): ZeppelinComponent = project.getComponent(ZeppelinComponent::class.java)
    }
}