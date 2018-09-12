package org.intellij.plugin.zeppelin.scala.runner

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.worksheet.cell.{CellDescriptor, RunCellActionBase, WorksheetCellExternalIdProvider}
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCompilerUtil.{RunCustom, WorksheetCompileRunRequest}

/**
  * Action which run cells in Zeppelin
  *
  * @param cellDescriptor - a zeppelin paragraph
  */
class RunZeppelinAction(cellDescriptor: CellDescriptor) extends RunCellActionBase(cellDescriptor) {
  var project: Project = _

  override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
    project = anActionEvent.getProject
    super.actionPerformed(anActionEvent)
  }

  override def convertToRunRequest(): WorksheetCompileRunRequest = {
    val element = cellDescriptor.getElement.get
    val id = ZeppelinCustomRunner.RUNNER_ID + "\n" +
      WorksheetCellExternalIdProvider.getSuitable(element).getOrElse("")
    RunCustom(id, project, cellDescriptor.getCellText)
  }
}