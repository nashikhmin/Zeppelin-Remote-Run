package jetbrains.zeppelin.toolwindow

import jetbrains.zeppelin.utils.ZeppelinOutput

/**
  * Implementation of the output for the tests
  */
class TextLogger extends ZeppelinOutput {
  override def printMessage(msg: String): Unit = println(msg)

  override def printError(msg: String): Unit = println(msg)
}
