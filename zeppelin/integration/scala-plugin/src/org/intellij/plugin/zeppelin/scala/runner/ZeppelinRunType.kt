package org.intellij.plugin.zeppelin.scala.runner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.dependency.dependency.ZeppelinInterpreterDependencies
import org.intellij.plugin.zeppelin.scala.worksheet.WorksheetSynchronizer
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinWorksheetSettingsDialog
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.worksheet.cell.CellDescriptor
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetExternalRunType
import org.jetbrains.plugins.scala.worksheet.ui.WorksheetEditorPrinter
import scala.Function1
import scala.Option
import scala.Some
import scala.runtime.BoxedUnit

/**
 * Zeppelin Worksheet Run Type extension
 */
class ZeppelinRunType : WorksheetExternalRunType() {
    override fun createPrinter(editor: Editor, file: ScalaFile): Option<WorksheetEditorPrinter> = Option.empty()
    override fun createRunCellAction(cellDescriptor: CellDescriptor): Option<AnAction>? {
        return Some(RunZeppelinAction(cellDescriptor))
    }

    override fun getName(): String = "Zeppelin"
    override fun isReplRunType(): Boolean = false
    override fun isUsesCell(): Boolean = true
    override fun onSettingsConfirmed(file: PsiFile): Unit {
        ZeppelinInterpreterDependencies(file.project).invokeImportUserDependencies()
        WorksheetSynchronizer.synchronize(file)
    }

    override fun showAdditionalSettingsPanel(): Option<Function1<PsiFile, BoxedUnit>>? {
        return Some(Function1 { psiFile: PsiFile ->
            val dialog = ZeppelinWorksheetSettingsDialog(psiFile)
            dialog.showAndGet()
            BoxedUnit.UNIT
        })
    }
}