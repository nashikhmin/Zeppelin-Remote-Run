package org.intellij.plugin.zeppelin.utils

import org.intellij.plugin.zeppelin.idea.toolwindow.TextLogger


object ZeppelinLogger {
  var output: ZeppelinOutput = new TextLogger

  def initOutput(output: ZeppelinOutput): Unit = this.output = output

  def printError(msg: String): Unit = output.printError("[ERROR] " + msg)

  def printMessage(msg: String): Unit = output.printMessage(if (msg.endsWith("\n")) msg.dropRight(1) else msg)

  def printSuccess(msg: String): Unit = output.printMessage("[SUCCESS] " + msg)
}


trait ZeppelinOutput {
  def printError(msg: String)

  def printMessage(msg: String)
}