package org.intellij.plugin.zeppelin.service

import org.intellij.plugin.zeppelin.models.Interpreter
import org.intellij.plugin.zeppelin.models.ZeppelinException

class InterpreterException(private val interpreter: Interpreter) : ZeppelinException() {
    override val message: String
        get() = "Reason: " + (interpreter.errorReason ?: "unavailable")
}

class InterpreterNotFoundException(private val id: String) : ZeppelinException() {
    override val message: String
        get() = "An interpreter with id $id is not found"
}

class NotebookException : ZeppelinException()

class ServiceIsUnavailableException : ZeppelinException() {
    override val message: String
        get() = "Service is unavailable, please restart the service"
}

class NoSelectedFilesException : ZeppelinException() {
    override val message: String
        get() = "Please, select the file for this operation"
}
