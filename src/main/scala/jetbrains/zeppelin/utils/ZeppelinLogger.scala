package jetbrains.zeppelin.utils

import jetbrains.zeppelin.toolwindow.TextLogger


object ZeppelinLogger {
  var output: ZeppelinOutput = new TextLogger

  def initOutput(output: ZeppelinOutput): Unit = {
    this.output = output
  }

  def printMessage(msg: String): Unit = output.printMessage(msg)

  def printError(msg: String): Unit = output.printError(msg)
}


trait ZeppelinOutput {
  def printMessage(msg: String)

  def printError(msg: String)
}
