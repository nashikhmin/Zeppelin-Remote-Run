package org.intellij.plugin.zeppelin.service.execution

interface ExecutionHandler {
    fun onError(msg: ExecutionResults)
    fun onOutput(data: OutputResponse, isAppend: Boolean)
    fun onProgress(percentage: Double)
    fun onSuccess(msg: ExecutionResults)
    fun onUpdateExecutionStatus(status: String)
    fun close()
}

interface ExecutionHandlerFactory {
    fun create(): ExecutionHandler
}