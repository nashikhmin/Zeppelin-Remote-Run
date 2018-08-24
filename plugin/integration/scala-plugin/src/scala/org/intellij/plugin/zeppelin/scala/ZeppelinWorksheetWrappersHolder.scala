package org.intellij.plugin.zeppelin.scala

import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.fileEditor.{FileEditorManagerEvent, FileEditorManagerListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.dependency.ImportZeppelinInterpreterDependencies
import jetbrains.zeppelin.idea.wizard.ZeppelinModuleUtils
import jetbrains.zeppelin.models.Interpreter
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinWorksheetFileSettings

class ZeppelinWorksheetWrappersHolder(project: Project) extends AbstractProjectComponent(project) {
  var currentInterpreter: Option[Interpreter] = None

  override def projectOpened(): Unit = {
    project.getMessageBus.connect(project)
      .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener {
        override def selectionChanged(event: FileEditorManagerEvent): Unit = {
          updateDependencies(event.getNewFile)
        }
      })
  }

  def updateDependencies(virtualFile: VirtualFile): Unit = {
    if (virtualFile == null) return
    if (!ZeppelinModuleUtils.isZeppelinModule(project, virtualFile)) return
    if (!isZeppelinWorksheet(virtualFile)) return

    val service = ZeppelinComponent.connectionFor(project).service
    val newInterpreter = service.getDefaultInterpreter
    if (currentInterpreter == newInterpreter) return
    ImportZeppelinInterpreterDependencies(project).invoke()
    currentInterpreter = newInterpreter
  }

  private def isZeppelinWorksheet(virtualFile: VirtualFile): Boolean = {
    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
    ZeppelinWorksheetFileSettings.isZeppelinWorksheet(psiFile)
  }
}

object ZeppelinWorksheetWrappersHolder {
  def connectionFor(project: Project): ZeppelinWorksheetWrappersHolder = {
    project
      .getComponent(classOf[ZeppelinWorksheetWrappersHolder])
  }
}