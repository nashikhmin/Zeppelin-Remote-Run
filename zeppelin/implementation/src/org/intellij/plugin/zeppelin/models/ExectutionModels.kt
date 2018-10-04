package org.intellij.plugin.zeppelin.models

data class ExecuteContext(val text: String, val noteId: String="", val paragraphId: String="")

data class ExecutionMessage(val code: String, val data: String)