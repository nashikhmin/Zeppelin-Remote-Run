package jetbrains.zeppelin.toolwindow


import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.openapi.project.Project

/**
  * Console that handle all zeppelin messages
  *
  * @param project - owner of this console
  */
class ZeppelinConsole(project: Project) extends ConsoleViewImpl(project, true)
