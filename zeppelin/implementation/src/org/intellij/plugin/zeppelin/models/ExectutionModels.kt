package org.intellij.plugin.zeppelin.models

data class ExecuteContext(val text: String, val noteId: String="", val paragraphId: String="")

data class ExecutionOperation(val id: String, val executeContext: ExecuteContext, val operations: List<ExecutionMessage>)

data class ExecutionMessage(val code: String, val data: String)