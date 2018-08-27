package jetbrains.zeppelin.service

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import jetbrains.zeppelin.constants.ZeppelinConstants

/**
  * Extension point which define a notebook id fore current opened file
  */
trait FileNotebookHolder {
  def contains(psiFile: PsiFile): Boolean

  def notebookId(psiFile: PsiFile): String
}

object FileNotebookHolder {
  val Id: String = ZeppelinConstants.PLUGIN_ID + ".fileNotebookHolder"
  val EP_NAME: ExtensionPointName[FileNotebookHolder] =
    ExtensionPointName.create[FileNotebookHolder](Id)

  def getAll: Array[FileNotebookHolder] = EP_NAME.getExtensions
}
