package org.intellij.scala

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.worksheet.cell.{CellDescriptor, RunCellActionBase}
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCompilerUtil
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCompilerUtil.RunCustom

class RunZeppelinAction(cellDescriptor: CellDescriptor) extends RunCellActionBase(cellDescriptor) {
  var project: Project = _

  override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
    project = anActionEvent.getProject
    super.actionPerformed(anActionEvent)
  }

  override def convertToRunRequest(cellText: String): WorksheetCompilerUtil.WorksheetCompileRunRequest = {
    RunCustom(ZeppelinCustomRunner.RUNNER_ID, project, cellText)
  }
}