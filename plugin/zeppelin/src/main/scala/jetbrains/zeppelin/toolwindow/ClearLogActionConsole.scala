package jetbrains.zeppelin.toolwindow

import com.intellij.execution.ui.ConsoleView
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.project.DumbAwareAction

/**
  * Console action to clear logs
  *
  * @param console - console that will be cleaned
  */
class ClearLogActionConsole(var console: ConsoleView) extends
  DumbAwareAction("Clear All", "Clear the contents of the zeppelin logs", AllIcons
    .Actions.GC) {
  override def update(e: AnActionEvent): Unit = {
    val editor = e.getData(CommonDataKeys.EDITOR)
    e.getPresentation.setEnabled(editor != null && editor.getDocument.getTextLength > 0)
  }

  override def actionPerformed(e: AnActionEvent): Unit = {
    console.clear()
  }
}
