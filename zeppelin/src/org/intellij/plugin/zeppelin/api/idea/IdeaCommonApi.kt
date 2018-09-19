package org.intellij.plugin.zeppelin.api.idea

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.vfs.VirtualFile

/**
 * Common methods for work with IDEA API
 */
object IdeaCommonApi {
    /**
     * Get a module of the current open file
     *
     * @param project - a current project
     * @return a current module
     */
    fun getCurrentModule(project: Project): Module? {
        val currentFile: VirtualFile = FileEditorManagerEx.getInstanceEx(project).currentFile!!
        return FileIndexFacade.getInstance(project).getModuleForFile(currentFile)
    }
}