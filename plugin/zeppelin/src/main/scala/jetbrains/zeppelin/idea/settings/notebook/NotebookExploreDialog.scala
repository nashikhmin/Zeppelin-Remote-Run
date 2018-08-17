package jetbrains.zeppelin.idea.settings.notebook

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.{JComponent, SwingConstants}
import jetbrains.zeppelin.components.ZeppelinComponent

import scala.collection.JavaConverters._

class NotebookExploreDialog(project: Project) extends DialogWrapper(project) {
  val title = s"Notebook browser"
  private val myPanel = new NotebookBrowserForm()

  setTitle(title)
  setButtonsAlignment(SwingConstants.CENTER)
  init()

  override def createCenterPanel(): JComponent = myPanel.getContentPane

  override def doOKAction(): Unit = {
    val connection = ZeppelinComponent.connectionFor(project)
    val actionService = connection.service

    val names = myPanel.getNotebookNames
    super.doOKAction()
  }

  override def init(): Unit = {
    super.init()
    updateNotebooksList()
  }

  def updateNotebooksList(): Unit = {
    myPanel.initDataModel(List("Nama", "Dadada", "Rustavelli").asJava)
  }
}