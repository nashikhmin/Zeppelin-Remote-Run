package org.intellij.plugin.zeppelin.extensionpoints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants

/**
 * Extension point which define a notebook id fore current opened file
 */
interface FileNotebookHolder {
    fun contains(psiFile: PsiFile): Boolean
    fun getNotebookId(psiFile: PsiFile): String

    companion object {
        private val Id: String = ZeppelinConstants.PLUGIN_ID + ".fileNotebookHolder"
        private val EP_NAME: ExtensionPointName<FileNotebookHolder> = ExtensionPointName.create<FileNotebookHolder>(
                Id)

        fun getAll(): List<FileNotebookHolder> = EP_NAME.extensions.toList()
    }
}
