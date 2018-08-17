package org.intellij.plugin.zeppelin.scala.runner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import jetbrains.zeppelin.dependency.ImportZeppelinInterpreterDependencies
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.worksheet.cell.CellDescriptor
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetExternalRunType
import org.jetbrains.plugins.scala.worksheet.ui.WorksheetEditorPrinter

class ZeppelinRunType extends WorksheetExternalRunType {
  override def createPrinter(editor: Editor, file: ScalaFile): Option[WorksheetEditorPrinter] = None

  override def createRunCellAction(cellDescriptor: CellDescriptor): Option[AnAction] = {
    Option(new RunZeppelinAction(cellDescriptor))
  }

  override def getName: String = "Zeppelin cells"

  override def isReplRunType: Boolean = false

  override def isUsesCell: Boolean = true

  override def onSettingsConfirmed(file: PsiFile, isGlobal: Boolean): Unit = {
    ImportZeppelinInterpreterDependencies(file
      .getProject).invoke()
  }

  override def showAdditionalSettingsPanel(): Option[() => Unit] = {
    Some(() => {
      //    new NotebookExploreDialog()
    })
  }
}