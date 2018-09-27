package org.intellij.plugin.zeppelin.scala.worksheet.settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.FileAttribute
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCompilerUtil.RunCustom
import org.jetbrains.plugins.scala.worksheet.settings.{WorksheetExternalRunType, WorksheetFileSettings}
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings.SerializableWorksheetAttributes.SerializableInFileAttribute

/**
  * Setting for Zeppelin worksheets
  *
  * @param file - a psi file with worksheet
  */
class ZeppelinWorksheetFileSettings(file: PsiFile) extends WorksheetFileSettings(file) {
  private val WORKSHEET_LINKED_NOTEBOOK_ID = new FileAttribute("ZeppelinWorksheetLinkedNotebook", 1, true)

  def getLinkedNotebookId: String = getSetting(WORKSHEET_LINKED_NOTEBOOK_ID, "")

  def setLinkedNotebookId(linkedNotebookId: String): Unit = setSetting(WORKSHEET_LINKED_NOTEBOOK_ID, linkedNotebookId)

  private def setSetting[T](attr: FileAttribute, value: T)(implicit ev: SerializableInFileAttribute[T]): Unit = {
    ev.writeAttribute(attr, file, value)
  }

  private def getSetting[T](attr: FileAttribute, orDefault: => T)(implicit ev: SerializableInFileAttribute[T]): T = {
    ev.readAttribute(attr, file).getOrElse(orDefault)
  }
}

class ScalaWorksheetSettingsFile extends WorksheetSettingsFile {

  override def getLinkedNotebookId(file: PsiFile): String = new ZeppelinWorksheetFileSettings(file).getLinkedNotebookId

  override def setLinkedNotebookId(file: PsiFile, notebookId: String): Unit = {
    new ZeppelinWorksheetFileSettings(file)
      .setLinkedNotebookId(notebookId)
  }

  override def getRunType(id: String, project: Project, data: String): RunCustom = RunCustom(id,project,data)
}