package org.intellij.plugin.zeppelin.service.execution

import java.util.concurrent.atomic.AtomicBoolean

trait ExecutionHandler {
  def onError(msg: ExecutionResults)

  def onOutput(data: OutputResponse, isAppend: Boolean)

  def onProgress(percentage: Double): Unit

  def onSuccess(msg: ExecutionResults)

  def onUpdateExecutionStatus(status: String): Unit

  def isCompleted: AtomicBoolean
}

trait ExecutionHandlerFactory {
  def create():ExecutionHandler
}