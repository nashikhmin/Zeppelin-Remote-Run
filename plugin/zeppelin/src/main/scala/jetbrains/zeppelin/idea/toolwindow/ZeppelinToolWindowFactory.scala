package jetbrains.zeppelin.idea.toolwindow

import com.intellij.openapi.actionSystem.{ActionManager, DefaultActionGroup}
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.{ToolWindow, ToolWindowFactory}
import com.intellij.ui.content.{Content, ContentFactory}
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.idea.toolwindow.actions._
import jetbrains.zeppelin.utils.ZeppelinLogger

/**
  * Factory that creates a Zeppelin tool window
  */
class ZeppelinToolWindowFactory extends ToolWindowFactory {

  override def createToolWindowContent(project: Project, toolWindow: ToolWindow): Unit = {
    toolWindow.getContentManager.addContent(createLogPanel(project))
    toolWindow.getContentManager.addContent(createInterpretersPanel(project))

    addAutoUpdate(project)
  }

  override def init(toolWindow: ToolWindow): Unit = {
    toolWindow.setStripeTitle("Zeppelin")
  }

  private def addAutoUpdate(project: Project): Unit = {
    ZeppelinComponent.connectionFor(project).updateInterpreterList(force = true)

    val connection = project.getMessageBus.connect(project)
    connection
      .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
        override def selectionChanged(event: FileEditorManagerEvent): Unit = {
          ZeppelinComponent.connectionFor(project).updateInterpreterList()
        }
      })
  }

  private def createInterpretersPanel(project: Project): Content = {
    val panel = new SimpleToolWindowPanel(false, true)

    val interpretersView = ZeppelinComponent.connectionFor(project).interpretersView
    panel.setContent(interpretersView)
    val toolbar = createInterpretersToolbar(project, interpretersView)
    panel.setToolbar(toolbar.getComponent)
    val content = ContentFactory.SERVICE.getInstance.createContent(panel, "Interpreters", true)
    Disposer.register(project, interpretersView)
    content
  }

  private def createInterpretersToolbar(project: Project, interpreters: InterpretersView) = {
    val group = new DefaultActionGroup
    group.add(new RefreshInterpretersAction())
    val toolbar = ActionManager.getInstance.createActionToolbar("left", group, false)
    toolbar.setTargetComponent(interpreters.getComponent)
    toolbar
  }

  private def createLogPanel(project: Project): Content = {
    val panel = new SimpleToolWindowPanel(false, true)
    val console = new ZeppelinConsole(project)
    ZeppelinLogger.initOutput(console)
    panel.setContent(console)
    val toolbar = createLogToolbar(project, console)
    panel.setToolbar(toolbar.getComponent)
    val content = ContentFactory.SERVICE.getInstance.createContent(panel, "Log", true)
    Disposer.register(project, console)
    content
  }

  private def createLogToolbar(project: Project, console: ZeppelinConsole) = {
    val group = new DefaultActionGroup
    group.add(new RefreshInterpretersAction())
    group.add(new RunCodeAction(project))
    group.addSeparator()
    group.add(new OpenGlobalSettingsFormAction())
    group.addSeparator()
    group.add(new ClearLogActionConsole(console))
    val toolbar = ActionManager.getInstance.createActionToolbar("left", group, false)
    toolbar.setTargetComponent(console.getComponent)
    toolbar
  }
}

object ZeppelinToolWindowFactory {
  val ID = "zeppelin-shell-toolwindow"
}