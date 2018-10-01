package org.intellij.plugin.zeppelin.service.execution

import java.util.concurrent.atomic.AtomicBoolean

/**
 * An executor for one Zeppelin Paragraph Run
 *
 * @param id - an id of an executing paragraph
 * @param outputHandler - a handler which get executions results
 */
class TaskExecutor(val id: String, private val outputHandler: ExecutionHandler) {
    fun isCompleted(): AtomicBoolean = outputHandler.isCompleted()

    fun appendOutput(output: OutputResponse) = outputHandler.onOutput(output, true)

    fun progress(progressResponse: ProgressResponse) = outputHandler.onProgress(progressResponse.progress / 100.0)

    fun paragraph(paragraphResponse: ParagraphResponse) {
        val status: String = paragraphResponse.status
        outputHandler.onUpdateExecutionStatus(status)
        when (status) {
            "FINISHED" -> {
                val results: ExecutionResults = paragraphResponse.results!!
                outputHandler.onSuccess(results)
            }
            "ERROR" -> {
                val results: ExecutionResults = paragraphResponse.results!!
                outputHandler.onError(results)
            }
        }
    }

    fun updateOutput(output: OutputResponse) {
        outputHandler.onOutput(output, false)
    }
}