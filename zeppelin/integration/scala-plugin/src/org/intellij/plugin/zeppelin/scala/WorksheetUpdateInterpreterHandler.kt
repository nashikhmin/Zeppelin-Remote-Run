package org.intellij.plugin.zeppelin.scala

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.plugin.zeppelin.extensionpoints.UpdateInterpreterHandler

open class WorksheetUpdateInterpreterHandler : UpdateInterpreterHandler {
    override fun updateInterpreter(project: Project) {
        val currentFile: VirtualFile = runReadAction {
            FileEditorManagerEx.getInstanceEx(project).currentFile
        } ?: return
        val holder = ZeppelinWorksheetWrappersHolder.connectionFor(project)
        holder.updateDependencies(currentFile,force = true)
    }
}