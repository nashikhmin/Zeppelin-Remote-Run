package org.intellij.plugin.zeppelin.service.execution

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.extensionpoints.TableOutputHandler
import org.intellij.plugin.zeppelin.utils.ZeppelinLogger
import org.intellij.plugin.zeppelin.utils.createBgIndicator
import java.util.concurrent.atomic.AtomicBoolean

class GuiExecutionHandler(private val project: Project) : ExecutionHandler {
    private val isCompletedValue: AtomicBoolean = AtomicBoolean(false)
    private var executionProgress: ExecutionProgress = ExecutionProgress()

    init {
        init()
    }


    private data class ExecutionProgress(val status: String = "", val percentage: Double = 0.0)

    override fun close() {
        isCompletedValue.set(true)
    }

    override fun onError(msg: ExecutionResults) {
        ZeppelinLogger.printError(ZeppelinConstants.PARAGRAPH_ERROR)
        msg.msg.forEach { it -> ZeppelinLogger.printError(it.data) }
        isCompletedValue.set(true)
    }

    override fun onOutput(data: OutputResponse, isAppend: Boolean) {
        if (data.data.isEmpty()) return
        ZeppelinLogger.printMessage(data.data)
    }

    override fun onProgress(percentage: Double) {
        executionProgress = executionProgress.copy(percentage = percentage)
    }

    override fun onSuccess(msg: ExecutionResults) {
        msg.msg.forEach { it ->
            when (it.type) {
                "TABLE" -> {
                    val handler: TableOutputHandler = TableOutputHandler.getHandler()
                    handler.handle(project, it.data)
                }
            }
            ZeppelinLogger.printMessage(ZeppelinConstants.PARAGRAPH_COMPLETED)
        }
    }

    override fun onUpdateExecutionStatus(status: String) {
        executionProgress = executionProgress.copy(status = status)
    }

    private fun init() {
        val manager: ProgressManager = ProgressManager.getInstance()
        val task: Backgroundable = object : Backgroundable(project, "Zeppelin Run", false) {
            override fun run(progressIndicator: ProgressIndicator) {
                progressIndicator.text = "Run paragraph on Zeppelin..."
                while (!isCompletedValue.get()) {
                    progressIndicator.text2 = executionProgress.status
                    progressIndicator.fraction = executionProgress.percentage
                    Thread.sleep(PROGRESS_UPDATE_TIME)
                }
            }
        }
        manager.runProcessWithProgressAsynchronously(task, createBgIndicator(project, "Zeppelin execution"))
    }

    companion object {
        private const val PROGRESS_UPDATE_TIME = 200L
    }
}

class GuiExecutionHandlerFactory(private val project: Project) : ExecutionHandlerFactory {
    override fun create(): ExecutionHandler = GuiExecutionHandler(project)
}