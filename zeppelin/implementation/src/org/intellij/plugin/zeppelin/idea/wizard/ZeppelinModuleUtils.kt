package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants


/**
 * Common utils for work with Zeppelin modules
 */
object ZeppelinModuleUtils {
    /**
     * Validate that the selected file is in a Zeppelin module
     *
     * @param project     - a current project
     * @param virtualFile - a opened file
     * @return is Zeppelin module
     */
    fun isZeppelinModule(project: Project, virtualFile: VirtualFile): Boolean {
        val module: Module = FileIndexFacade.getInstance(project).getModuleForFile(virtualFile) ?: return false
        return module.moduleTypeName == ZeppelinConstants.MODULE_ID
    }
}