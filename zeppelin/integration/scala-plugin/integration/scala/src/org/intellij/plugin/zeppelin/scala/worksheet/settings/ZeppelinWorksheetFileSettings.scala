package org.intellij.plugin.zeppelin.scala.worksheet.settings

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.FileAttribute
import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.scala.runner.ZeppelinRunType
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.worksheet.processor.WorksheetCompilerUtil.RunCustom
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings.SerializableWorksheetAttributes.SerializableInFileAttribute

/**
  * Setting for Zeppelin worksheets
  *
  * @param file - a psi file with worksheet
  */
private class ZeppelinWorksheetFileSettings(file: PsiFile) extends WorksheetFileSettings(file) {

  import ZeppelinWorksheetFileSettings._

  def getLinkedNotebookId: String = getSetting(WORKSHEET_LINKED_NOTEBOOK_ID, "")

  def setLinkedNotebookId(linkedNotebookId: String): Unit = setSetting(WORKSHEET_LINKED_NOTEBOOK_ID, linkedNotebookId)

  private def setSetting[T](attr: FileAttribute, value: T)(implicit ev: SerializableInFileAttribute[T]): Unit = {
    ev.writeAttribute(attr, file, value)
  }

  private def getSetting[T](attr: FileAttribute, orDefault: => T)(implicit ev: SerializableInFileAttribute[T]): T = {
    ev.readAttribute(attr, file).getOrElse(orDefault)
  }
}

private object ZeppelinWorksheetFileSettings {
  private val WORKSHEET_LINKED_NOTEBOOK_ID = new FileAttribute("ZeppelinWorksheetLinkedNotebook", 1, true)

  def hasNotebookId(file: PsiFile): Boolean = {
    isZeppelinWorksheet(file) && getLinkedNotebookId(file) != ""
  }

  def getLinkedNotebookId(file: PsiFile): String = new ZeppelinWorksheetFileSettings(file).getLinkedNotebookId

  def isZeppelinWorksheet(file: PsiFile): Boolean = {
    file.isInstanceOf[ScalaFile] &&
      new ZeppelinWorksheetFileSettings(file).getRunType
        .isInstanceOf[ZeppelinRunType]
  }

  def setLinkedNotebookId(file: PsiFile, notebookId: String): Unit = {
    new ZeppelinWorksheetFileSettings(file)
      .setLinkedNotebookId(notebookId)
  }
}

class ZeppelinWorksheetSettings extends WorksheetSettingsFile {

  override def getLinkedNotebookId(file: PsiFile): String = ZeppelinWorksheetFileSettings.getLinkedNotebookId(file)

  override def setLinkedNotebookId(file: PsiFile, notebookId: String): Unit = ZeppelinWorksheetFileSettings
    .setLinkedNotebookId(file, notebookId)

  override def getRunType(id: String, project: Project, data: String): RunCustom = RunCustom(id, project, data)
}