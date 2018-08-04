package jetbrains.zeppelin.ui.toolwindow.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnActionEvent, Presentation}
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.vfs.VirtualFile
import jetbrains.zeppelin.components.ZeppelinComponent
import jetbrains.zeppelin.utils.dependency.ZeppelinDependenciesManager

/**
  * Refresh a list of available interpreters on Zeppelin.
  */
class UpdateDependenciesAction extends DumbAwareAction {
  val templatePresentation: Presentation = getTemplatePresentation
  templatePresentation.setIcon(AllIcons.Actions.Download)
  templatePresentation.setText("Update Dependencies")

  override def actionPerformed(event: AnActionEvent): Unit = {
    val project = event.getProject

    val currentFile: VirtualFile = FileEditorManagerEx.getInstanceEx(project).getCurrentFile
    val module: Module = FileIndexFacade.getInstance(project)
      .getModuleForFile(currentFile)


    val connection = ZeppelinComponent.connectionFor(event.getProject)
    val service = connection.service
    val interpreter = service.getDefaultInterpreter
    val jars = interpreter.dependencies.map(_.groupArtifactVersion)
    ZeppelinDependenciesManager.addUserInterpreterLibrary(module, jars)
  }
}