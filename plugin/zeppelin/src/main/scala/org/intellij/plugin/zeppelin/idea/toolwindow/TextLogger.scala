package org.intellij.plugin.zeppelin.idea.toolwindow

import org.intellij.plugin.zeppelin.utils.ZeppelinOutput

/**
  * Implementation of the output for the tests
  */
class TextLogger extends ZeppelinOutput {
  override def printError(msg: String): Unit = println(msg)

  override def printMessage(msg: String): Unit = println(msg)
}
