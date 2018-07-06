package jetbrains.zeppelin.service

import jetbrains.zeppelin.api.Interpreter

class InterpreterException(interpreter: Interpreter) extends Exception {
  override def getMessage: String = super.getMessage + interpreter.errorReason
}

class InterpreterNotFoundException(id: String) extends Exception {
  override def getMessage: String = s"An interpreter with id ${id} is not found"
}

class NotebookException() extends Exception

class ServiceIsUnavailableException() extends Exception {
  override def getMessage: String = "Service is unavailable, please restart the service"
}