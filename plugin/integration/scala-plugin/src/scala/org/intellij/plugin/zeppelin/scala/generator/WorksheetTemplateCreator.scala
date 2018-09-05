package org.intellij.plugin.zeppelin.scala.generator

import java.io.{File, PrintWriter}

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi.PsiManager
import org.intellij.plugin.zeppelin.extensionpoints.TemplateFileCreator
import org.intellij.plugin.zeppelin.scala.constants.ZeppelinScalaConstants
import org.intellij.plugin.zeppelin.scala.runner.ZeppelinRunType
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinWorksheetFileSettings
import org.jetbrains.plugins.scala.worksheet.cell.CellManager

/**
  * A creator of a zeppelin worksheet template
  */
class WorksheetTemplateCreator extends TemplateFileCreator{
  /**
    * Create a Zeppelin Worksheet
    * @param srcPath - a path to source Path
    * @param project - an IDEA project
    */
  override def create(srcPath: String, project: Project): Unit = {
    val defaultNotebook = getName(srcPath)
    val file: File = createTemplateFile(defaultNotebook)

    val virtualFile: VirtualFile = LocalFileSystem.getInstance.refreshAndFindFileByIoFile(file)
    FileEditorManager.getInstance(project).openFile(virtualFile, true)

    initAsZeppelinWorksheet(project, virtualFile)
  }

  private def initAsZeppelinWorksheet(project: Project,
                                      virtualFile: VirtualFile): Unit = {
    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
    val settings = new ZeppelinWorksheetFileSettings(psiFile)
    val runType = new ZeppelinRunType
    settings.setRunType(runType)
    CellManager.installCells(psiFile)
    runType.onSettingsConfirmed(psiFile)
  }

  private def createTemplateFile(defaultNotebook: String) = {
    val file = new File(defaultNotebook)
    file.createNewFile()

    val writer = new PrintWriter(file, "UTF-8")
    writer.println(ZeppelinScalaConstants.TEMPLATE_TEXT)
    writer.close()
    file
  }

  private def getName(srcPath: String) = {
    srcPath + "/untitled.sc"
  }
}