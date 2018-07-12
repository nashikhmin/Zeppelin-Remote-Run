package jetbrains.zeppelin.utils

import jetbrains.zeppelin.toolwindow.TextLogger


object ZeppelinLogger {
  var output: ZeppelinOutput = new TextLogger

  def initOutput(output: ZeppelinOutput): Unit = {
    this.output = output
  }

  def printMessage(msg: String): Unit = output.printMessage(msg)

  def printSuccess(msg: String): Unit = output.printMessage("[SUCCESS] " + msg)

  def printError(msg: String): Unit = output.printError("[ERROR] " + msg)
}


trait ZeppelinOutput {
  def printMessage(msg: String)

  def printError(msg: String)
}
