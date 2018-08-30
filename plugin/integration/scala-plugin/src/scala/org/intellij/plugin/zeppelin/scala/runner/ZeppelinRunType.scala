package org.intellij.plugin.zeppelin.scala.runner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.dependency.ImportZeppelinInterpreterDependencies
import org.intellij.plugin.zeppelin.scala.worksheet.WorksheetSynchronizer
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinWorksheetSettingsDialog
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.worksheet.cell.CellDescriptor
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetExternalRunType
import org.jetbrains.plugins.scala.worksheet.ui.WorksheetEditorPrinter

/**
  * Zeppelin Worksheet Run Type
  */
class ZeppelinRunType extends WorksheetExternalRunType {
  override def createPrinter(editor: Editor, file: ScalaFile): Option[WorksheetEditorPrinter] = None

  override def createRunCellAction(cellDescriptor: CellDescriptor): Option[AnAction] = {
    Option(new RunZeppelinAction(cellDescriptor))
  }

  override def getName: String = "Zeppelin"

  override def isReplRunType: Boolean = false

  override def isUsesCell: Boolean = true

  override def onSettingsConfirmed(file: PsiFile): Unit = {
    ImportZeppelinInterpreterDependencies(file.getProject).invoke()
    WorksheetSynchronizer.synchronize(file)
  }

  override def showAdditionalSettingsPanel(): Option[PsiFile => Unit] = {
    Some((psiFile: PsiFile) => {
      val dialog = new ZeppelinWorksheetSettingsDialog(psiFile)
      dialog.showAndGet()
    })
  }
}