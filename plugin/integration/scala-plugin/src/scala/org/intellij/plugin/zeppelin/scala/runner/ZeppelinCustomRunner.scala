package org.intellij.plugin.zeppelin.scala.runner

import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.jetbrains.plugins.scala.worksheet.processor.{WorksheetCompilerUtil, WorksheetCustomRunner}

/**
  * A Zeppelin cell runner
  */
class ZeppelinCustomRunner extends WorksheetCustomRunner {
  override def canHandle(request: WorksheetCompilerUtil.RunCustom): Boolean = {
    request.id ==
      ZeppelinCustomRunner.RUNNER_ID
  }

  override def handle(request: WorksheetCompilerUtil.RunCustom): Unit = {
    val executeCode = request.data
    val connection = ZeppelinComponent.connectionFor(request.project)
    connection.focusToLog()
    val service = connection.service
    service.runCode(executeCode)
  }
}

object ZeppelinCustomRunner {
  val RUNNER_ID = "Zeppelin"
}