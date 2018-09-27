package org.intellij.plugin.zeppelin.scala.worksheet.extensions

import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.extensionpoints.FileNotebookHolder
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinFileSettings

class WorksheetNotebookHolder : FileNotebookHolder {
    override fun getNotebookId(psiFile: PsiFile): String =
            ZeppelinFileSettings.getLinkedNotebookId(psiFile)

    override fun contains(
            psiFile: PsiFile): Boolean = ZeppelinFileSettings.hasNotebookId(psiFile)
}