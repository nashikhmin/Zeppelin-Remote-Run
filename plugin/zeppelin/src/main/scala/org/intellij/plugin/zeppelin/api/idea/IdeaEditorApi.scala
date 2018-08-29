package org.intellij.plugin.zeppelin.api.idea

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx

/**
  * API for working with current Editor
  */
trait IdeaEditorApi {
  def currentEditor(anActionEvent: AnActionEvent): Option[Editor] = {
    Option(FileEditorManagerEx.getInstanceEx(anActionEvent.getProject).getSelectedTextEditor)
  }

  def currentSelectedText(editor: Option[Editor]): String = {
    if (editor.isEmpty) return ""
    val selectionModel = editor.head.getSelectionModel
    val blockStarts = selectionModel.getBlockSelectionStarts
    val blockEnds = selectionModel.getBlockSelectionEnds
    editor.head.getDocument.getCharsSequence.subSequence(blockStarts(0), blockEnds(0)).toString
  }
}