package jetbrains.zeppelin.toolwindow

import com.intellij.openapi.actionSystem.{ActionManager, DefaultActionGroup}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.{ToolWindow, ToolWindowFactory}
import com.intellij.ui.content.ContentFactory
import jetbrains.zeppelin.toolwindow.actions.{RunCodeAction, UpdateJarOnZeppelin}
import jetbrains.zeppelin.utils.ZeppelinLogger

/**
  * Factory that creates a Zeppelin tool window
  */
class ZeppelinToolWindowFactory extends ToolWindowFactory {
  override def createToolWindowContent(project: Project, toolWindow: ToolWindow): Unit = {
    val panel = new SimpleToolWindowPanel(false, true)

    val console = new ZeppelinConsole(project)
    ZeppelinLogger.initOutput(console)
    panel.setContent(console)
    val toolbar = createToolbar(project, console)
    panel.setToolbar(toolbar.getComponent)


    val content = ContentFactory.SERVICE.getInstance.createContent(panel, "", true)

    toolWindow.getContentManager.addContent(content)

    Disposer.register(project, console)
  }

  private def createToolbar(project: Project, console: ZeppelinConsole) = {
    val group = new DefaultActionGroup
    group.add(new ClearLogActionConsole(console))
    group.add(new RunCodeAction(project))
    group.add(new UpdateJarOnZeppelin())
    val toolbar = ActionManager.getInstance.createActionToolbar("left", group, false)
    toolbar.setTargetComponent(console.getComponent)
    toolbar
  }
}