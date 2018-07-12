package jetbrains.zeppelin.external.compile

import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.psi.util.PsiUtilBase

//TODO: it is just stub and do not using now
object LanguageCompilerService {
  def compile(projectPath: String, event: AnActionEvent): String = {
    val context = event.getDataContext
    val editor: Editor = FileEditorManagerEx.getInstanceEx(event.getProject).getSelectedTextEditor
    if (editor == null) throw new Exception

    val file = PsiUtilBase.getPsiFileInEditor(editor, CommonDataKeys.PROJECT.getData(context))
    val language = file.getLanguage
    val extensions: Array[LanguageCompiler] = Extensions.getExtensions(LanguageCompiler.EP_NAME, event.getProject)
    extensions.find(_.isSupport(language)).getOrElse(LanguageCompilerStub())
    "stub"
  }
}
