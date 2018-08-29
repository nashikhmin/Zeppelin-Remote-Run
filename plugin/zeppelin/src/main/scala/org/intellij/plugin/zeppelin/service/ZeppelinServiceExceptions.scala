package org.intellij.plugin.zeppelin.service

import org.intellij.plugin.zeppelin.models.Interpreter

class InterpreterException(interpreter: Interpreter) extends Exception {
  override def getMessage: String = "Reason: " + interpreter.errorReason.getOrElse("unavailable")
}

class InterpreterNotFoundException(id: String) extends Exception {
  override def getMessage: String = s"An interpreter with id $id is not found"
}

class NotebookException() extends Exception

class ServiceIsUnavailableException() extends Exception {
  override def getMessage: String = "Service is unavailable, please restart the service"
}

class NoSelectedFilesException() extends Exception {
  override def getMessage: String = "Please, select the file for this operation"
}