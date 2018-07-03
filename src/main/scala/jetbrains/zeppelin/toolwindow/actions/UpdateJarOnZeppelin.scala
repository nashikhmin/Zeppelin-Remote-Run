package jetbrains.zeppelin.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.project.DumbAwareAction
import jetbrains.zeppelin.components.ZeppelinConnection

/**
  * Upload project jar to Zeppelin server
  */
class UpdateJarOnZeppelin extends DumbAwareAction {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.Actions.Compile)
  templatePresentation.setText("Compile project and send to Zeppelin")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val zeppelinService = ZeppelinConnection.connectionFor(event.getProject).service
    val projectPath = event.getProject.getBasePath
    zeppelinService.updateJar(projectPath)
  }
}