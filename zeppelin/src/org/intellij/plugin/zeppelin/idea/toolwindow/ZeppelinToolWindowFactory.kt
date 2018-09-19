package org.intellij.plugin.zeppelin.idea.toolwindow

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.messages.MessageBusConnection
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.idea.toolwindow.actions.ClearLogActionConsole
import org.intellij.plugin.zeppelin.idea.toolwindow.actions.OpenGlobalSettingsFormAction
import org.intellij.plugin.zeppelin.idea.toolwindow.actions.RefreshInterpretersAction
import org.intellij.plugin.zeppelin.idea.toolwindow.actions.RunCodeAction
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
 * Factory that creates a Zeppelin tool window
 */
class ZeppelinToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.contentManager.addContent(createLogPanel(project))
        toolWindow.contentManager.addContent(createInterpretersPanel(project))
        addAutoUpdate(project)
    }

    override fun init(toolWindow: ToolWindow) {
        toolWindow.stripeTitle = "Zeppelin"
    }

    private fun addAutoUpdate(project: Project) {
        ZeppelinComponent.connectionFor(project).updateInterpreterList(true)
        val connection: MessageBusConnection = project.messageBus.connect(project)
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) = ZeppelinComponent.connectionFor(project).updateInterpreterList(true)
        })
    }

    private fun createInterpretersPanel(project: Project): Content {
        val panel = SimpleToolWindowPanel(false, true)
        val interpretersView: InterpretersView = ZeppelinComponent.connectionFor(project).interpretersView
        panel.setContent(interpretersView)
        val toolbar: ActionToolbar = createInterpretersToolbar(interpretersView)
        panel.setToolbar(toolbar.component)
        val content: Content = ContentFactory.SERVICE.getInstance().createContent(panel, "Interpreters", true)
        Disposer.register(project, interpretersView)
        return content
    }

    private fun createInterpretersToolbar(interpreters: InterpretersView): ActionToolbar {
        val group = DefaultActionGroup()
        group.add(RefreshInterpretersAction())
        val toolbar: ActionToolbar = ActionManager.getInstance().createActionToolbar("left", group, false)
        toolbar.setTargetComponent(interpreters)
        return toolbar
    }

    private fun createLogPanel(project: Project): Content {
        val panel = SimpleToolWindowPanel(false, true)
        val console = ZeppelinConsole(project)
        ZeppelinLogger.output = console
        panel.setContent(console)
        val toolbar: ActionToolbar = createLogToolbar(console)
        panel.setToolbar(toolbar.component)
        val content: Content = ContentFactory.SERVICE.getInstance().createContent(panel, "Log", true)
        Disposer.register(project, console)
        return content
    }

    private fun createLogToolbar(console: ZeppelinConsole): ActionToolbar {
        val group = DefaultActionGroup()
        group.add(RefreshInterpretersAction())
        group.add(RunCodeAction())
        group.addSeparator()
        group.add(OpenGlobalSettingsFormAction())
        group.addSeparator()
        group.add(ClearLogActionConsole(console))
        val toolbar: ActionToolbar = ActionManager.getInstance().createActionToolbar("left", group, false)
        toolbar.setTargetComponent(console.component)
        return toolbar
    }

    companion object {
        const val ID: String = "zeppelin-shell-toolwindow"
    }
}