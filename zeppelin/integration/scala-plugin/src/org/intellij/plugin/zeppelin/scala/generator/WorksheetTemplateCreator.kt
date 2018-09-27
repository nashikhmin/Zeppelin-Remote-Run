package org.intellij.plugin.zeppelin.scala.generator

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.intellij.plugin.zeppelin.extensionpoints.TemplateFileCreator
import org.intellij.plugin.zeppelin.scala.constants.ZeppelinScalaConstants
import org.intellij.plugin.zeppelin.scala.runner.ZeppelinRunType
import org.jetbrains.plugins.scala.worksheet.cell.`CellManager$`
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings
import java.io.File
import java.io.PrintWriter

open class WorksheetTemplateCreator : TemplateFileCreator {
    /**
     * Create a Zeppelin Worksheet
     * @param sourceFolder - a path to source Path
     * @param project - an IDEA project
     */
    override fun create(sourceFolder: String, project: Project) {
        val defaultNotebook: String = getName(sourceFolder)
        val file: File = createTemplateFile(defaultNotebook)
        val virtualFile: VirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)!!
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
        initAsZeppelinWorksheet(project, virtualFile)
    }

    private fun initAsZeppelinWorksheet(project: Project, virtualFile: VirtualFile): Unit {
        val psiFile: PsiFile = PsiManager.getInstance(project).findFile(virtualFile)!!
        val settings = WorksheetFileSettings(psiFile)
        val runType = ZeppelinRunType()
        settings.runType = runType

        `CellManager$`.`MODULE$`.installCells(psiFile)
        runType.onSettingsConfirmed(psiFile)
    }

    private fun createTemplateFile(defaultNotebook: String): File {
        val file = File(defaultNotebook)
        file.createNewFile()
        val writer = PrintWriter(file, "UTF-8")
        writer.println(ZeppelinScalaConstants.TEMPLATE_TEXT)
        writer.close()
        return file
    }

    private fun getName(srcPath: String): String {
        return "$srcPath/untitled.sc"
    }
}