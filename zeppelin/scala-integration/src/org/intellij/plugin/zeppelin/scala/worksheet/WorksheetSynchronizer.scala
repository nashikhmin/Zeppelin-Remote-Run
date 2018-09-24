package org.intellij.plugin.zeppelin.scala.worksheet

import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.models.NotebookNotFoundException
import org.intellij.plugin.zeppelin.scala.constants.ZeppelinScalaConstants
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinWorksheetFileSettings
import org.jetbrains.plugins.scala.worksheet.cell.{CellManager, WorksheetCellExternalIdProvider}

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
  def synchronize(psiFile: PsiFile): Unit = {
    if (!ZeppelinWorksheetFileSettings.hasNotebookId(psiFile)) return
    val notebookId = ZeppelinWorksheetFileSettings.getLinkedNotebookId(psiFile)
    val project = psiFile.getProject
    val service = ZeppelinComponent.connectionFor(project).service
    val notebook = service.getNotebookById(notebookId)
    if (notebook.isEmpty) throw NotebookNotFoundException(notebookId)
    val paragraphId = ZeppelinScalaConstants.PARAGRAPH_WITH_EXTERNAL_ID
    val paragraphs = notebook.get.paragraphs.map(it => paragraphId + it.id + it.text.getOrElse("\n")).mkString("\n")
    WorksheetCellExternalIdProvider.getSuitable(psiFile)
    CellManager.replaceAll(psiFile, paragraphs)
  }
}