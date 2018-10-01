package org.intellij.plugin.zeppelin.idea.toolwindow

import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import org.intellij.plugin.zeppelin.utils.ZeppelinOutput

/**
 * Console that handle all zeppelin messages
 *
 * @param project - owner of this console
 */
open class ZeppelinConsole(project: Project) : ConsoleViewImpl(project, true), ZeppelinOutput {
    override fun printError(msg: String): Unit = print(msg + "\n", ConsoleViewContentType.ERROR_OUTPUT)
    override fun printMessage(msg: String): Unit = print(msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT)
}