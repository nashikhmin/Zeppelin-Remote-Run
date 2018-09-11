package org.intellij.plugin.zeppelin.idea.wizard

import java.security.cert.PKIXRevocationChecker.Option

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
  def isZeppelinModule(project: Project, virtualFile: VirtualFile): Boolean = {
    val module = Option(FileIndexFacade.getInstance(project).getModuleForFile(virtualFile))
    if (module.isEmpty) return false
    module.get.getModuleTypeName == ZeppelinConstants.MODULE_ID
  }
}