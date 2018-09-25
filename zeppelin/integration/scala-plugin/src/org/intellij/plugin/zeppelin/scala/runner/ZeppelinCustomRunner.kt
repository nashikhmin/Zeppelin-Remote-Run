package org.intellij.plugin.zeppelin.scala.runner

import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCompilerUtil.RunCustom
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCustomRunner

/**
 * A Zeppelin cell runner
 */
open class ZeppelinCustomRunner : WorksheetCustomRunner() {
    override fun canHandle(request: RunCustom): Boolean {
        val id: String = request.id().split("\n")[0]
        return id == ZeppelinCustomRunner.RUNNER_ID
    }

    override fun handle(request: RunCustom) {
        val executeCode: String = request.data()
        val id: String? = parseId(request.id())
        val connection: ZeppelinComponent = ZeppelinComponent.connectionFor(request.project())
        connection.focusToLog()
        val service = connection.service
        service.runCode(executeCode, id)
    }

    private fun parseId(s: String): String? {
        val ids: List<String> = s.split("\n")
        if (ids.size < 2) return null
        val rawId: String = ids[1]
        if (rawId.isEmpty()) return null
        return rawId
    }

    companion object {
        const val RUNNER_ID: String = "Zeppelin"
    }
}