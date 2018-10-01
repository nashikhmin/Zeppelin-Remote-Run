package org.intellij.plugin.zeppelin.utils

import org.intellij.plugin.zeppelin.idea.toolwindow.TextLogger

object ZeppelinLogger {
    var output: ZeppelinOutput = TextLogger()

    fun printError(msg: String): Unit = output.printError("[ERROR] $msg")
    fun printMessage(msg: String): Unit = output.printMessage(if (msg.endsWith("\n")) msg.dropLast(1) else msg)
    fun printSuccess(msg: String): Unit = output.printMessage("[SUCCESS] $msg")
}

interface ZeppelinOutput {
    fun printError(msg: String)
    fun printMessage(msg: String)
}