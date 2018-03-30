package jetbrains.zeppelin.api.idea

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx

/**
  * API for working with current Editor
  */
trait IdeaEditorApi {
  def currentEditor(anActionEvent: AnActionEvent): Editor = {
    FileEditorManagerEx.getInstanceEx(anActionEvent.getProject).getSelectedTextEditor
  }

  def currentSelectedText(editor: Editor): String = {
    val selectionModel = editor.getSelectionModel
    val blockStarts = selectionModel.getBlockSelectionStarts
    val blockEnds = selectionModel.getBlockSelectionEnds
    editor.getDocument.getCharsSequence.subSequence(blockStarts(0), blockEnds(0)).toString
  }
}