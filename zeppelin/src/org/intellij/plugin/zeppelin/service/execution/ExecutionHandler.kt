package org.intellij.plugin.zeppelin.service.execution

import java.util.concurrent.atomic.AtomicBoolean

interface ExecutionHandler {
    fun onError(msg: ExecutionResults)
    fun onOutput(data: OutputResponse, isAppend: Boolean)
    fun onProgress(percentage: Double)
    fun onSuccess(msg: ExecutionResults)
    fun onUpdateExecutionStatus(status: String)
    fun isCompleted(): AtomicBoolean
}

interface ExecutionHandlerFactory {
    fun create(): ExecutionHandler
}