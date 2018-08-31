package org.intellij.plugin.zeppelin.idea.settings.interpreter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants

/**
  * Updating after changing interpreters
  */
trait UpdateInterpreterHandler {
  def updateInterpreter(project: Project)
}

object UpdateInterpreterHandler {
  val EP_NAME: ExtensionPointName[UpdateInterpreterHandler] =
    ExtensionPointName.create[UpdateInterpreterHandler](ZeppelinConstants.PLUGIN_ID + ".updateInterpreterHandler")

  def getAll: Array[UpdateInterpreterHandler] = EP_NAME.getExtensions
}