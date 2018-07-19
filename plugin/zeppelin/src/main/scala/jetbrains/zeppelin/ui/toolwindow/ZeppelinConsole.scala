package jetbrains.zeppelin.ui.toolwindow


import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import jetbrains.zeppelin.utils.ZeppelinOutput

/**
  * Console that handle all zeppelin messages
  *
  * @param project - owner of this console
  */
class ZeppelinConsole(project: Project) extends ConsoleViewImpl(project, true) with ZeppelinOutput {
  def printError(msg: String): Unit = print(msg + "\n", ConsoleViewContentType.ERROR_OUTPUT)

  def printMessage(msg: String): Unit = print(msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT)
}