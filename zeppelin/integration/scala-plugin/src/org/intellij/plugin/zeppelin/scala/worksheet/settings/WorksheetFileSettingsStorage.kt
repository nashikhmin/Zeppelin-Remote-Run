package org.intellij.plugin.zeppelin.scala.worksheet.settings

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.models.ZeppelinException
import org.intellij.plugin.zeppelin.scala.runner.ZeppelinRunType
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCompilerUtil
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings

object ZeppelinFileSettings {
    fun isZeppelinWorksheet(file: PsiFile): Boolean {
        return file is ScalaFile && WorksheetFileSettings(file).runType is ZeppelinRunType
    }

    fun hasNotebookId(file: PsiFile): Boolean {
        return isZeppelinWorksheet(file) && getLinkedNotebookId(file) != ""
    }

    fun getLinkedNotebookId(file: PsiFile): String {
        return WorksheetSettingsFile.getAvailable().getLinkedNotebookId(file)
    }

    fun setLinkedNotebookId(file: PsiFile, id: String) =
            WorksheetSettingsFile.getAvailable().setLinkedNotebookId(file, id)

    fun getRunType(id: String, project: Project, data: String): WorksheetCompilerUtil.RunCustom {
        return WorksheetSettingsFile.getAvailable().getRunType(id, project, data)
    }
}

interface WorksheetSettingsFile {
    fun getLinkedNotebookId(file: PsiFile): String

    fun setLinkedNotebookId(file: PsiFile, id: String)

    fun getRunType(id: String, project: Project, data: String): WorksheetCompilerUtil.RunCustom

    companion object {
        fun getAvailable() = this.EP_NAME.extensions.firstOrNull() ?: throw ZeppelinException(
                "Worksheet settings implementation is not available")

        private val ID: String = ZeppelinConstants.PLUGIN_ID + ".worksheetSettingsFile"
        private val EP_NAME = ExtensionPointName.create<WorksheetSettingsFile>(ID)
    }
}