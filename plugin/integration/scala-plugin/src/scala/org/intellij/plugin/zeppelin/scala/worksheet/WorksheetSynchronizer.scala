package org.intellij.plugin.zeppelin.scala.worksheet

import com.intellij.psi.PsiFile
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.models.NotebookNotFoundException
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinWorksheetFileSettings

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
    val notebookId = ZeppelinWorksheetFileSettings.getLinkedNotebookId(psiFile)
    if (notebookId == "") return
    val project = psiFile.getProject
    val service = ZeppelinComponent.connectionFor(project).service
    val notebook = service.getNotebookById(notebookId)
    if (notebook.isEmpty) throw NotebookNotFoundException(notebookId)
    val paragraphs = notebook.get.paragraphs


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