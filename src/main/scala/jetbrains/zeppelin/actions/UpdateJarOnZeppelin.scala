package jetbrains.zeppelin.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import jetbrains.zeppelin.components.ZeppelinConnection

class UpdateJarOnZeppelin extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val zeppelinService = ZeppelinConnection.connectionFor(event.getProject).service
    val projectPath = event.getProject.getBasePath
    zeppelinService.updateJar(projectPath)
  }
}