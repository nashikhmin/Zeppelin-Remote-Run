package org.intellij.plugin.zeppelin.scala.runner

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinFileSettings
import org.jetbrains.plugins.scala.worksheet.cell.CellDescriptor
import org.jetbrains.plugins.scala.worksheet.cell.RunCellActionBase
import org.jetbrains.plugins.scala.worksheet.cell.WorksheetCellExternalIdProvider
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCompilerUtil.WorksheetCompileRunRequest

/**
 * Action which run cells in Zeppelin
 *
 * @param cellDescriptor - a zeppelin paragraph
 */
class RunZeppelinAction(private val cellDescriptor: CellDescriptor) : RunCellActionBase(cellDescriptor) {
    override fun convertToRunRequest(): WorksheetCompileRunRequest {
        val element = cellDescriptor.element.get()
        val paragraphId: String = WorksheetCellExternalIdProvider.getSuitable(element).get()
        val id: String = ZeppelinCustomRunner.RUNNER_ID + "\n" + paragraphId

        return ZeppelinFileSettings.getRunType(id, project!!, cellDescriptor.cellText)
    }

    var project: Project? = null

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        project = anActionEvent.project
        super.actionPerformed(anActionEvent)
    }
}