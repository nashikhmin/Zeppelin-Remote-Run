package org.intellij.plugin.zeppelin.api.idea

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx

/**
 * API for working with current Editor
 */
object IdeaEditorApi {
    fun currentEditor(anActionEvent: AnActionEvent): Editor? {
        return FileEditorManagerEx.getInstanceEx(anActionEvent.project ?: return null).selectedTextEditor
    }

    fun currentSelectedText(editor: Editor?): String {
        val selectionModel: SelectionModel = editor?.selectionModel ?: return ""
        val blockStarts: IntArray = selectionModel.blockSelectionStarts
        val blockEnds: IntArray = selectionModel.blockSelectionEnds
        return editor.document.charsSequence.subSequence(blockStarts[0], blockEnds[0]).toString()
    }
}