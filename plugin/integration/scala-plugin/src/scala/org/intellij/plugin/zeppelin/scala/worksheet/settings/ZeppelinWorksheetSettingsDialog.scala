package org.intellij.plugin.zeppelin.scala.worksheet.settings

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiFile
import javax.swing.JComponent

class ZeppelinWorksheetSettingsDialog(psiFile: PsiFile) extends DialogWrapper(psiFile.getProject) {
  override def createCenterPanel(): JComponent = ???
}
