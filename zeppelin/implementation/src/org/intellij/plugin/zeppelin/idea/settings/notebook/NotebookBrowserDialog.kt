package org.intellij.plugin.zeppelin.idea.settings.notebook

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.models.Notebook
import javax.swing.JComponent
import javax.swing.SwingConstants

class NotebookBrowserDialog(val project: Project) : DialogWrapper(project) {
    private val myPanel = NotebookBrowserForm()
    private val actionService = ZeppelinComponent.connectionFor(project).service

    init {
        title = "Notebook browser"
        setButtonsAlignment(SwingConstants.CENTER)
        init()
    }

    override fun createCenterPanel(): JComponent = myPanel.contentPane

    override fun doOKAction() {
        val currentNotebooks = myPanel.notebooks
        val notebooks = actionService.updateNotebooksTo(currentNotebooks).sortedBy { it.name }
        myPanel.notebooks = notebooks
        super.doOKAction()
    }

    override fun init() {
        super.init()
        initDataModel()
    }

    fun initDataModel() {
        val notebooks = getOriginalNotebooks().sortedBy { it.name }
        myPanel.initDataModel(notebooks)
    }

    /**
     * Open from and get selected result
     *
     * @return tuple (is value selected, selected value)
     */
    fun openAndGetResult(): Pair<Boolean, Notebook?> {
        if (!showAndGet()) return Pair(false, null)

        val value = myPanel.selectedValue ?: return Pair(false, null)
        if (value.name == ZeppelinComponent.connectionFor(project).getZeppelinSettings().defaultNotebookName) {
            return Pair(true, null)
        }
        return Pair(true, value)
    }

    private fun getOriginalNotebooks() = actionService.getNotebooksList()
}