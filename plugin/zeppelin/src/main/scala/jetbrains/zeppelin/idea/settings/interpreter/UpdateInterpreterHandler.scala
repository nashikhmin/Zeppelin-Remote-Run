package jetbrains.zeppelin.idea.settings.interpreter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

/**
  * Updating after changing interpreters
  */
trait UpdateInterpreterHandler {
  def updateInterpreter(project: Project)
}

object UpdateInterpreterHandler {
  val EP_NAME: ExtensionPointName[UpdateInterpreterHandler] =
    ExtensionPointName.create[UpdateInterpreterHandler]("org.jetbrains.scala.zeppelin.updateInterpreterHandler")

  def getAll: Array[UpdateInterpreterHandler] = EP_NAME.getExtensions
}