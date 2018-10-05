package org.intellij.plugin.zeppelin.service.execution

import org.intellij.plugin.zeppelin.api.remote.ZeppelinApi
import org.intellij.plugin.zeppelin.api.remote.websocket.MessageHandler
import org.intellij.plugin.zeppelin.api.remote.websocket.ResponseCode
import org.intellij.plugin.zeppelin.api.remote.websocket.WsResponseMessage
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.models.ExecuteContext
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger
import java.util.concurrent.ConcurrentHashMap

/**
 * Service which execute code on Zeppelin and manager queue of execution tasks and messages
 *
 * @param api - an instance of zeppelin API
 */
open class ZeppelinExecutionManager(private val api: ZeppelinApi,
                                    private val executionHandlerFactory: ExecutionHandlerFactory) {
    init {
        api.registerHandler(ResponseCode.PROGRESS, object : MessageHandler {
            override fun handle(result: WsResponseMessage) {
                handleParagraphProgress(result)
            }
        })

        api.registerHandler(ResponseCode.PARAGRAPH, object : MessageHandler {
            override fun handle(result: WsResponseMessage) {
                handleParagraph(result)
            }
        })

        api.registerHandler(ResponseCode.PARAGRAPH_APPEND_OUTPUT, object : MessageHandler {
            override fun handle(result: WsResponseMessage) {
                handleParagraphAppendOutput(result)
            }
        })

        api.registerHandler(ResponseCode.PARAGRAPH_UPDATE_OUTPUT, object : MessageHandler {
            override fun handle(result: WsResponseMessage) {
                handleParagraphUpdateOutput(result)
            }
        })
    }

    private val tasks: ConcurrentHashMap<String, TaskExecutor> = ConcurrentHashMap()

    fun execute(executeContext: ExecuteContext) {
        ZeppelinLogger.printMessage(ZeppelinConstants.PARAGRAPH_RUNNED.format(executeContext.text))
        val task = TaskExecutor(executeContext.paragraphId, executionHandlerFactory.create())
        tasks[executeContext.paragraphId] = task
        api.runCode(executeContext)
        clean()
    }

    fun isExecutingNow(id: String): Boolean = !(tasks[id]?.isCompleted() ?: true)

    private fun clean() {
        val completedTasksIds = tasks.toMap().entries.filter { it.value.isCompleted() }.map { it.key }
        completedTasksIds.forEach { tasks.remove(it) }
    }

    private fun getIfExistsTaskExecutor(id: String): TaskExecutor? {
        return tasks[id]
    }

    private fun handleParagraph(message: WsResponseMessage) {
        val paragraphResponse = ExecutionModelConverter.getParagraphResponse(message.data)
        val taskExecutor: TaskExecutor = getIfExistsTaskExecutor(paragraphResponse.id) ?: return
        taskExecutor.paragraph(paragraphResponse)
    }

    private fun handleParagraphAppendOutput(message: WsResponseMessage) {
        val updateOutput = ExecutionModelConverter.getOutputResult(message.data)
        val taskExecutor: TaskExecutor = getIfExistsTaskExecutor(updateOutput.paragraphId) ?: return
        taskExecutor.appendOutput(updateOutput)
    }

    private fun handleParagraphProgress(message: WsResponseMessage) {
        val progress = ExecutionModelConverter.getProgressResponse(message.data)
        val taskExecutor: TaskExecutor = getIfExistsTaskExecutor(progress.id) ?: return
        taskExecutor.progress(progress)
    }

    private fun handleParagraphUpdateOutput(message: WsResponseMessage) {
        val updateOutput = ExecutionModelConverter.getOutputResult(message.data)
        val taskExecutor: TaskExecutor = getIfExistsTaskExecutor(updateOutput.paragraphId) ?: return
        taskExecutor.updateOutput(updateOutput)
    }
}