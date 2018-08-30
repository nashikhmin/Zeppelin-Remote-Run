package org.intellij.plugin.zeppelin.scala.worksheet

import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.models.NotebookNotFoundException
import org.jetbrains.plugins.scala.worksheet.cell.CellManager

/**
  * Class, which response for synchronization of worksheets with Zeppelin notebooks
  */
object WorksheetSynchronizer {
  /**
    * Syncronize worksheet with linked notebook
    *
    * @param psiFile - a file with worksheet
    * @throws NotebookNotFoundException - if Zeppelin haven't a linked notebook
    */
  def synchronize(psiFile: PsiFile): Unit = {

    //    if (!ZeppelinWorksheetFileSettings.hasNotebookId(psiFile)) return
    //    val notebookId = ZeppelinWorksheetFileSettings.getLinkedNotebookId(psiFile)
    //    val project = psiFile.getProject
    //    val service = ZeppelinComponent.connectionFor(project).service
    //    val notebook = service.getNotebookById(notebookId)
    //    if (notebook.isEmpty) throw NotebookNotFoundException(notebookId)
    //    val paragraphs = notebook.get.paragraphs

    //    CellManager.createCell(psiFile,0,"val a=3\nval b=4\n")
    val cells = CellManager.getInstance(psiFile.getProject).getCells(psiFile)
    val virtualFile = psiFile.getVirtualFile
    //    val document = FileDocumentManager.getInstance().getDocument(virtualFile)
    ////    ThreadRun.inWriteAction {
    ////      document.setText("")
    ////    }
    //    var i = 0
    //    paragraphs.foreach(paragraph => {
    //      val text = ("//##\n" + paragraph.text).split("\n")
    //      text.foreach(line => {
    //        ThreadRun.inWriteAction {
    //          document.insertString(i, line)
    //        }
    //        i = i + 1
    //      })
    //    })
  }
}