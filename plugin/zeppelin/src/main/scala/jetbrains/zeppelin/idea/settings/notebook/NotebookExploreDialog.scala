package jetbrains.zeppelin.idea.settings.notebook

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.{JComponent, SwingConstants}
import jetbrains.zeppelin.components.ZeppelinComponent

import scala.collection.JavaConverters._

class NotebookExploreDialog(project: Project) extends DialogWrapper(project) {
  val title = s"Notebook browser"
  private val LOG = Logger.getInstance(getClass)
  private val myPanel = new NotebookBrowserForm()

  setTitle(title)
  setButtonsAlignment(SwingConstants.CENTER)
  init()

  override def createCenterPanel(): JComponent = myPanel.getContentPane

  override def doOKAction(): Unit = {
    val originalNotebooks = getOriginalNotebooks.toSet
    val currentNotebooks = myPanel.getNotebooks.asScala.toSet

    val deletedNotebooks = originalNotebooks.diff(currentNotebooks)
    val addedNotebooks = currentNotebooks.diff(originalNotebooks)

    val msg = s"${addedNotebooks.size} notebooks will be added, ${deletedNotebooks.size} will be removed"
    LOG.info(msg)
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

  def openAndGetResult(): Option[String] = {
    if (showAndGet()) {
      Option(myPanel.getSelectedValue)
    }
    else {
      None
    }
  }

  private def getOriginalNotebooks = {
    val connection = ZeppelinComponent.connectionFor(project)
    val actionService = connection.service
    actionService.getNotebooksList()
  }
}