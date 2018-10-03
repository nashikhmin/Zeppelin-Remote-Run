package org.intellij.plugin.zeppelin.scala

import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.dependency.ZeppelinInterpreterDependencies
import org.intellij.plugin.zeppelin.models.Interpreter
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinFileSettings

@Suppress("ComponentNotRegistered")
class ZeppelinWorksheetWrappersHolder(private val project: Project) : AbstractProjectComponent(project) {
    var currentInterpreter: Interpreter? = null
    override fun projectOpened(): Unit {
        project.messageBus.connect(project)
                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                    override fun selectionChanged(event: FileEditorManagerEvent) {
                        val virtualFile = event.newFile ?: return
                        updateDependencies(virtualFile)
                    }
                })
    }

    fun updateDependencies(virtualFile: VirtualFile, force: Boolean = false) {
        if (!isZeppelinWorksheet(virtualFile)) return
        val service = ZeppelinComponent.connectionFor(project).service
        val newInterpreter = service.getDefaultInterpreter()
        if (!force && currentInterpreter == newInterpreter) return
        currentInterpreter = newInterpreter
        ZeppelinInterpreterDependencies(project).invokeImportUserDependencies()
    }

    private fun isZeppelinWorksheet(virtualFile: VirtualFile): Boolean {
        val psiFile: PsiFile = PsiManager.getInstance(project).findFile(virtualFile)!!
        return ZeppelinFileSettings.isZeppelinWorksheet(psiFile)
    }

    companion object {
        fun connectionFor(project: Project): ZeppelinWorksheetWrappersHolder =
                project.getComponent(ZeppelinWorksheetWrappersHolder::class.java)
    }
}