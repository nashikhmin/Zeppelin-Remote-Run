package org.intellij.plugin.zeppelin.scala.cell

import com.intellij.psi.PsiElement
import org.intellij.plugin.zeppelin.scala.constants.ZeppelinScalaConstants
import org.jetbrains.plugins.scala.worksheet.cell.WorksheetCellExternalIdProvider
import scala.Option
import scala.Some

class ZeppelinIdProvider : WorksheetCellExternalIdProvider() {
  override fun canHandle(startElement: PsiElement): Boolean {
    return startElement.text.contains(ZeppelinScalaConstants.EXTERNAL_ID)
  }
  override fun getId(startElement: PsiElement): Option<String>? {
     val text: String = startElement.text
     val start: Int = text.indexOf(ZeppelinScalaConstants.EXTERNAL_ID) + ZeppelinScalaConstants.EXTERNAL_ID.length
     val id: String = text.substring(start)
    return Some(id)
  }
}