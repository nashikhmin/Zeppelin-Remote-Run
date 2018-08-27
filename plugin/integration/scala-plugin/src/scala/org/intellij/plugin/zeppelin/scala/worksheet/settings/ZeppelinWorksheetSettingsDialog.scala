package org.intellij.plugin.zeppelin.scala.worksheet.settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiFile
import javax.swing.{JComponent, SwingConstants}
import jetbrains.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.scala.constants.ZeppelinScalaConstants

class ZeppelinWorksheetSettingsDialog(psiFile: PsiFile) extends DialogWrapper(psiFile.getProject) {
  private val title: String = ZeppelinScalaConstants.ZEPPELIN_WORKSHEET_SETTINGS_TITLE
  private val project: Project = psiFile.getProject
  private val myPanel = new ZeppelinWorksheetSettingsForm(project)

  setTitle(title)
  setButtonsAlignment(SwingConstants.CENTER)
  init()

  override def createCenterPanel(): JComponent = myPanel.getContentPane

  override def doOKAction(): Unit = {
    val notebook = myPanel.getNotebook
    if (notebook != null) {
      ZeppelinWorksheetFileSettings.setLinkedNotebookId(psiFile, notebook.id)
    }
    else {
      ZeppelinWorksheetFileSettings.setLinkedNotebookId(psiFile, "")
    }
    super.doOKAction()
  }

  override def init(): Unit = {
    super.init()
    val linkedNotebookId = ZeppelinWorksheetFileSettings.getLinkedNotebookId(psiFile)
    val isSync = ZeppelinWorksheetFileSettings.hasNotebookId(psiFile)
    myPanel.setSyncNotebook(isSync)
    if (isSync) {
      val zeppelin = ZeppelinComponent.connectionFor(project).service
      val notebook = zeppelin.getNotebookById(linkedNotebookId)
      notebook.foreach(it => {
        myPanel.setNotebook(it)
      })
    }
  }
}