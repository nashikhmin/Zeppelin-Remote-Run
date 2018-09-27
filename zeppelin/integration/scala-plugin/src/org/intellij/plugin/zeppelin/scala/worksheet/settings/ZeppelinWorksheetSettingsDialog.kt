package org.intellij.plugin.zeppelin.scala.worksheet.settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.scala.constants.ZeppelinScalaConstants
import javax.swing.JComponent
import javax.swing.SwingConstants

open class ZeppelinWorksheetSettingsDialog(private val psiFile: PsiFile) : DialogWrapper(psiFile.project) {
    override fun getTitle(): String {
        return ZeppelinScalaConstants.ZEPPELIN_WORKSHEET_SETTINGS_TITLE
    }

    private val project: Project = psiFile.project
    private val myPanel: ZeppelinWorksheetSettingsForm = ZeppelinWorksheetSettingsForm(project)

    init {
        setTitle(title)
        setButtonsAlignment(SwingConstants.CENTER)
        init()
    }

    override fun createCenterPanel(): JComponent = myPanel.contentPane
    override fun doOKAction(): Unit {
        val notebook = myPanel.notebook
        if (notebook != null) {
            ZeppelinFileSettings.setLinkedNotebookId(psiFile, notebook.id)
        } else {
            ZeppelinFileSettings.setLinkedNotebookId(psiFile, "")
        }
        super.doOKAction()
    }

    override fun init(): Unit {
        super.init()
        val isSync = ZeppelinFileSettings.hasNotebookId(psiFile)
        myPanel.setSyncNotebook(isSync)
        if (isSync) {
            val linkedNotebookId = ZeppelinFileSettings.getLinkedNotebookId(psiFile)
            val zeppelin = ZeppelinComponent.connectionFor(project).service
            val notebook = zeppelin.getNotebookById(linkedNotebookId)
            notebook?.let { myPanel.notebook = it }
        }
    }
}