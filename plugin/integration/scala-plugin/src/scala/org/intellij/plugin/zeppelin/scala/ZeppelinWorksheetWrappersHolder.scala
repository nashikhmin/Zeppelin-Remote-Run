package org.intellij.plugin.zeppelin.scala

import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.fileEditor.{FileEditorManagerEvent, FileEditorManagerListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import jetbrains.zeppelin.api.Interpreter
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.dependency.ImportZeppelinInterpreterDependencies
import jetbrains.zeppelin.idea.wizard.ZeppelinModuleUtils

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

  private def isZeppelinWorksheet(virtualFile: VirtualFile): Boolean = {
    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
    Utils.isZeppelinWorksheet(psiFile)
  }

  private def updateDependencies(virtualFile: VirtualFile): Unit = {
    if (!ZeppelinModuleUtils.isZeppelinModule(project, virtualFile)) return
    if (!isZeppelinWorksheet(virtualFile)) return

    val service = ZeppelinComponent.connectionFor(project).service
    val newInterpreter = service.getDefaultInterpreter
    if (currentInterpreter == newInterpreter) return
    ImportZeppelinInterpreterDependencies(project).invoke()
    currentInterpreter = newInterpreter
  }
}