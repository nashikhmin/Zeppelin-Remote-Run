package jetbrains.zeppelin.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, PlatformDataKeys}
import com.intellij.openapi.ui.Messages

class TextBoxes extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val project = event.getData(PlatformDataKeys.PROJECT_CONTEXT)
    val txt = Messages.showInputDialog(project, "what is your name", "Input your name", Messages.getQuestionIcon)
    Messages.showMessageDialog(project, "Greeting", s"Hello ${txt}I'm glad to see you.", Messages.getInformationIcon)
  }
}