package jetbrains.zeppelin.idea.settings.notebook

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.{JComponent, SwingConstants}
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.models.Notebook

import scala.collection.JavaConverters._

class NotebookBrowserDialog(project: Project, defaultNotebook: Notebook) extends DialogWrapper(project) {
  val title = s"Notebook browser"
  private val myPanel = new NotebookBrowserForm()
  private val actionService = ZeppelinComponent.connectionFor(project).service

  setTitle(title)
  setButtonsAlignment(SwingConstants.CENTER)
  init()

  override def createCenterPanel(): JComponent = myPanel.getContentPane

  override def doOKAction(): Unit = {
    val currentNotebooks = myPanel.getNotebooks.asScala.toList
    val notebooks = actionService.updateNotebooksTo(currentNotebooks)
    myPanel.setNotebooks(notebooks.asJava)
    super.doOKAction()
  }

  override def init(): Unit = {
    super.init()
    initDataModel()
  }

  def initDataModel(): Unit = {
    val notebooks = getOriginalNotebooks
    myPanel.initDataModel(notebooks.asJava)
  }

  /**
    * Open from and get selected result
    *
    * @return tuple (is value selected, selected value)
    */
  def openAndGetResult(): (Boolean, Option[Notebook]) = {
    if (!showAndGet()) return (false, None)

    val value = myPanel.getSelectedValue
    if (value != null &&
      value.name == ZeppelinComponent.connectionFor(project).getZeppelinSettings.defaultNotebookName) {
      return (true, None)
    }
    (true, Option(value))

  }

  private def getOriginalNotebooks = actionService.getNotebooksList
}