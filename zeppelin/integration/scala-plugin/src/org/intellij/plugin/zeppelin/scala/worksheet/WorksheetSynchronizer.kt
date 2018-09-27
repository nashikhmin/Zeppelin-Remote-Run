package org.intellij.plugin.zeppelin.scala.worksheet

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.models.NotebookNotFoundException
import org.intellij.plugin.zeppelin.scala.constants.ZeppelinScalaConstants
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinFileSettings
import org.jetbrains.plugins.scala.worksheet.cell.WorksheetCellExternalIdProvider
import org.jetbrains.plugins.scala.worksheet.cell.`CellManager$`

/**
 * Class, which response for synchronization of worksheets with Zeppelin notebooks
 */
object WorksheetSynchronizer {
    /**
     * Synchronize worksheet with linked notebook
     *
     * @param psiFile - a file with worksheet
     * @throws NotebookNotFoundException - if Zeppelin haven't a linked notebook
     */
    fun synchronize(psiFile: PsiFile) {
        if (!ZeppelinFileSettings.hasNotebookId(psiFile)) return
        val notebookId: String = ZeppelinFileSettings.getLinkedNotebookId(psiFile)
        val project: Project = psiFile.project
        val service = ZeppelinComponent.connectionFor(project).service
        val notebook = service.getNotebookById(notebookId) ?: throw NotebookNotFoundException(notebookId)
        val paragraphId: String = ZeppelinScalaConstants.PARAGRAPH_WITH_EXTERNAL_ID
        val paragraphs = notebook.paragraphs.map { paragraphId + it.id + it.text }.joinToString("\n")
        WorksheetCellExternalIdProvider.getSuitable(psiFile)

        `CellManager$`.`MODULE$`.replaceAll(psiFile, paragraphs)
    }
}