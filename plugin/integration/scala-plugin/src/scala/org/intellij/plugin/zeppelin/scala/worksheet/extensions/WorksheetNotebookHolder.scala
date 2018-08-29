package org.intellij.plugin.zeppelin.scala.worksheet.extensions

import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinWorksheetFileSettings
import org.intellij.plugin.zeppelin.service.FileNotebookHolder

class WorksheetNotebookHolder extends FileNotebookHolder {
  override def contains(psiFile: PsiFile): Boolean = ZeppelinWorksheetFileSettings.hasNotebookId(psiFile)

  override def notebookId(psiFile: PsiFile): String = ZeppelinWorksheetFileSettings.getLinkedNotebookId(psiFile)
}
