package org.intellij.plugin.zeppelin.scala.runner

import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.jetbrains.plugins.scala.worksheet.processor.{WorksheetCompilerUtil, WorksheetCustomRunner}

/**
  * A Zeppelin cell runner
  */
class ZeppelinCustomRunner extends WorksheetCustomRunner {
  override def canHandle(request: WorksheetCompilerUtil.RunCustom): Boolean = {
    val id = request.id.split("\n")(0)
    id == ZeppelinCustomRunner.RUNNER_ID
  }

  override def handle(request: WorksheetCompilerUtil.RunCustom): Unit = {
    val executeCode = request.data
    val id = parseId(request.id)
    val connection = ZeppelinComponent.connectionFor(request.project)
    connection.focusToLog()
    val service = connection.service
    service.runCode(executeCode, id)
  }

  def parseId(s: String): Option[String] = {
    val ids = s.split("\n")
    if (ids.length<2) return  None
    val rawId = ids(1)
    if (rawId.isEmpty) None
    Some(rawId)
  }
}

object ZeppelinCustomRunner {
  val RUNNER_ID = "Zeppelin"
}