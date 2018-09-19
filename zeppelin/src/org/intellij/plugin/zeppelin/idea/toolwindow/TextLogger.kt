package org.intellij.plugin.zeppelin.idea.toolwindow

import org.intellij.plugin.zeppelin.utils.ZeppelinOutput

/**
 * Implementation of the output for the tests
 */
class TextLogger : ZeppelinOutput {
    override fun printError(msg: String): Unit = println(msg)
    override fun printMessage(msg: String): Unit = println(msg)
}