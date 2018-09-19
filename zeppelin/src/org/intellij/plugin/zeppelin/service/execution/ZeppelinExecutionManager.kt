package org.intellij.plugin.zeppelin.service.execution

import org.intellij.plugin.zeppelin.api.ZeppelinApi
import org.intellij.plugin.zeppelin.api.websocket.MessageHandler
import org.intellij.plugin.zeppelin.api.websocket.ResponseCode
import org.intellij.plugin.zeppelin.api.websocket.ResponseMessage
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.models.ExecuteContext
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Service which execute code on Zeppelin and manager queue of execution tasks and messages
 *
 * @param api - an instance of zeppelin API
 */
open class ZeppelinExecutionManager(private val api: ZeppelinApi,
                                    private val executionHandlerFactory: ExecutionHandlerFactory) {
    init {
        api.registerHandler(ResponseCode.PROGRESS, object : MessageHandler {
            override fun handle(result: ResponseMessage) {
                handleParagraphProgress(result)
            }
        })

        api.registerHandler(ResponseCode.PARAGRAPH, object : MessageHandler {
            override fun handle(result: ResponseMessage) {
                handleParagraph(result)
            }
        })

        api.registerHandler(ResponseCode.PARAGRAPH_APPEND_OUTPUT, object : MessageHandler {
            override fun handle(result: ResponseMessage) {
                handleParagraphAppendOutput(result)
            }
        })

        api.registerHandler(ResponseCode.PARAGRAPH_UPDATE_OUTPUT, object : MessageHandler {
            override fun handle(result: ResponseMessage) {
                handleParagraphUpdateOutput(result)
            }
        })
    }

    private val tasks: ConcurrentLinkedQueue<TaskExecutor> = ConcurrentLinkedQueue()

    fun execute(executeContext: ExecuteContext) {
        ZeppelinLogger.printMessage(ZeppelinConstants.PARAGRAPH_RUNNED.format(executeContext.text))
        val task = TaskExecutor(executeContext.paragraphId, executionHandlerFactory.create())
        tasks.add(task)
        api.runCode(executeContext)
        clean()
    }

    private fun clean() {
        var task: TaskExecutor? = tasks.peek()
        while (task != null && task.isCompleted().get()) {
            tasks.poll()
            task = tasks.peek()
        }
    }

    private fun getOrCreateTaskExecutor(id: String): TaskExecutor? {
        val tasksArray: Array<TaskExecutor> = tasks.toArray<TaskExecutor>(arrayOf())
        return tasksArray.find { it.id == id }
    }

    private fun handleParagraph(message: ResponseMessage) {
        val paragraphResponse = ExecutionModelConverter.getParagraphResponse(message.data)
        val taskExecutor: TaskExecutor = getOrCreateTaskExecutor(paragraphResponse.id) ?: return
        taskExecutor.paragraph(paragraphResponse)
    }

    private fun handleParagraphAppendOutput(message: ResponseMessage) {
        val updateOutput = ExecutionModelConverter.getOutputResult(message.data)
        val taskExecutor: TaskExecutor = getOrCreateTaskExecutor(updateOutput.paragraphId) ?: return
        taskExecutor.appendOutput(updateOutput)
    }

    private fun handleParagraphProgress(message: ResponseMessage) {
        val progress = ExecutionModelConverter.getProgressResponse(message.data)
        val taskExecutor: TaskExecutor = getOrCreateTaskExecutor(progress.id) ?: return
        taskExecutor.progress(progress)
    }

    private fun handleParagraphUpdateOutput(message: ResponseMessage) {
        val updateOutput = ExecutionModelConverter.getOutputResult(message.data)
        val taskExecutor: TaskExecutor = getOrCreateTaskExecutor(updateOutput.paragraphId) ?: return
        taskExecutor.updateOutput(updateOutput)
    }
}