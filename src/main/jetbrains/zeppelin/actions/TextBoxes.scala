package jetbrains.zeppelin.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, PlatformDataKeys}
import com.intellij.openapi.ui.Messages
import jetbrains.zeppelin.api.{RestAPI, ZeppelinApi}

import scala.util.Random

class TextBoxes extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val project = event.getData(PlatformDataKeys.PROJECT_CONTEXT)
    val restAPI = new RestAPI("localhost", 8080)
    val zeppelinApi = new ZeppelinApi(restAPI)
    zeppelinApi.login("user1", "password2")
    val notebook = zeppelinApi.createNotebook(s"PluginDir/${Random.alphanumeric.take(10).mkString}")
    Messages.showMessageDialog(project, s"Created id is ${notebook.id}", "We create", Messages.getInformationIcon)
  }
}