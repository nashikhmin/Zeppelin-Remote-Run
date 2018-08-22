package org.intellij.plugin.zeppelin.scala

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import jetbrains.zeppelin.idea.settings.interpreter.UpdateInterpreterHandler

/**
  * A handler which handle updating of interpreters in worksheet
  */
class WorksheetUpdateInterpreterHandler extends UpdateInterpreterHandler {
  override def updateInterpreter(project: Project): Unit = {
    val currentFile: VirtualFile = FileEditorManagerEx.getInstanceEx(project).getCurrentFile
    if (currentFile == null) return
    val holder = ZeppelinWorksheetWrappersHolder.connectionFor(project)
    holder.updateDependencies(currentFile)
  }
}