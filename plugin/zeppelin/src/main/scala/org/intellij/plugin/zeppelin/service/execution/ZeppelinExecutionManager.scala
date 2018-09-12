package org.intellij.plugin.zeppelin.service.execution

import java.util.concurrent.ConcurrentLinkedQueue

import org.intellij.plugin.zeppelin.api.ZeppelinApi
import org.intellij.plugin.zeppelin.api.websocket._
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.models.ExecuteContext
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger

/**
  * Service which execute code on Zeppelin and manager queue of execution tasks and messages
  *
  * @param api - an instance of zeppelin API
  */
class ZeppelinExecutionManager(api: ZeppelinApi, executionHandlerFactory: ExecutionHandlerFactory) {
  val tasks = new ConcurrentLinkedQueue[TaskExecutor]()
  init()

  def execute(executeContext: ExecuteContext): Unit = {
    ZeppelinLogger.printMessage(ZeppelinConstants.PARAGRAPH_RUNNED.format(executeContext.text))
    val task = TaskExecutor(executeContext.paragraphId, executionHandlerFactory.create())
    tasks.add(task)
    api.runCode(executeContext)
    clean()
  }

  private def clean(): Unit = {
    var task = tasks.peek()
    while (task != null && task.isCompleted.get()) {
      tasks.poll()
      task = tasks.peek()
    }
  }

  private def getOrCreateTaskExecutor(id: String): Option[TaskExecutor] = {
    val tasksArray = tasks.toArray[TaskExecutor](new Array[TaskExecutor](0))
    tasksArray.find(_.id == id)
  }

  private def handleParagraph(message: ResponseMessage): Unit = {
    val paragraphResponse = ExecutionModelConverter.getParagraphResponse(message.data)
    val taskExecutor = getOrCreateTaskExecutor(paragraphResponse.id)
    if (taskExecutor.isEmpty) return
    taskExecutor.get.paragraph(paragraphResponse)
  }

  private def handleParagraphAppendOutput(message: ResponseMessage): Unit = {
    val updateOutput = ExecutionModelConverter.getOutputResult(message.data)
    val taskExecutor = getOrCreateTaskExecutor(updateOutput.paragraphId)
    if (taskExecutor.isEmpty) return
    taskExecutor.get.appendOutput(updateOutput)
  }

  private def handleParagraphProgress(message: ResponseMessage): Unit = {
    val progress = ExecutionModelConverter.getProgressResponse(message.data)
    val taskExecutor = getOrCreateTaskExecutor(progress.id)
    if (taskExecutor.isEmpty) return
    taskExecutor.get.progress(progress)
  }

  private def handleParagraphUpdateOutput(message: ResponseMessage): Unit = {
    val updateOutput = ExecutionModelConverter.getOutputResult(message.data)
    val taskExecutor = getOrCreateTaskExecutor(updateOutput.paragraphId)
    if (taskExecutor.isEmpty) return
    taskExecutor.get.updateOutput(updateOutput)
  }

  private def init(): Unit = {
    api.registerHandler(ResponseCode.PROGRESS, (result: ResponseMessage) => handleParagraphProgress(result))
    api.registerHandler(ResponseCode.PARAGRAPH, (result: ResponseMessage) => handleParagraph(result))
    api.registerHandler(ResponseCode.PARAGRAPH_APPEND_OUTPUT,
      (result: ResponseMessage) => handleParagraphAppendOutput(result))
    api.registerHandler(ResponseCode.PARAGRAPH_UPDATE_OUTPUT,
      (result: ResponseMessage) => handleParagraphUpdateOutput(result))

  }
}

object ZeppelinExecutionManager {
  def apply(api: ZeppelinApi, executionHandlerFactory: ExecutionHandlerFactory): ZeppelinExecutionManager = {
    new ZeppelinExecutionManager(api, executionHandlerFactory)
  }
}
