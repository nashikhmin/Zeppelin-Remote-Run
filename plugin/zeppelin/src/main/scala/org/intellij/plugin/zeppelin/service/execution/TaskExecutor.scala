package org.intellij.plugin.zeppelin.service.execution

import java.util.concurrent.atomic.AtomicBoolean

/**
  * An executor for one Zeppelin Paragraph Run
  *
  * @param id - an id of an executing paragraph
  * @param outputHandler - a handler which get executions results
  */
class TaskExecutor(val id: String, outputHandler: ExecutionHandler) {
  def isCompleted: AtomicBoolean= outputHandler.isCompleted

  def appendOutput(output: OutputResponse): Unit = {
    outputHandler.onOutput(output, isAppend = true)
  }

  def progress(progressResponse: ProgressResponse): Unit = {
    outputHandler.onProgress(progressResponse.progress/100.0)
  }

  def paragraph(paragraphResponse: ParagraphResponse): Unit = {
    val status = paragraphResponse.status
    outputHandler.onUpdateExecutionStatus(status)

    status match {
      case "FINISHED" => {
        val results = paragraphResponse.results
        outputHandler.onSuccess(results.get)
      }
      case "ERROR" => {
        val results = paragraphResponse.results
        outputHandler.onError(results.get)
      }
      case _ => Unit
    }
  }

  def updateOutput(output: OutputResponse): Unit = {
    outputHandler.onOutput(output, isAppend = false)
  }
}

object TaskExecutor {
  def apply(id:String, outputHandler: ExecutionHandler): TaskExecutor = new TaskExecutor(id, outputHandler)
}
