package org.intellij.plugin.zeppelin.scala.cell

import com.intellij.psi.PsiElement
import org.intellij.plugin.zeppelin.scala.constants.ZeppelinScalaConstants
import org.jetbrains.plugins.scala.worksheet.cell.WorksheetCellExternalIdProvider

class ZeppelinIdProvider extends WorksheetCellExternalIdProvider {
  override def canHandle(startElement: PsiElement): Boolean = {
    startElement.getText.contains(ZeppelinScalaConstants.EXTERNAL_ID)
  }

  override def getId(startElement: PsiElement): Option[String] = {
    val text = startElement.getText
    val start = text.indexOf(ZeppelinScalaConstants.EXTERNAL_ID) + ZeppelinScalaConstants.EXTERNAL_ID.length
    val id = text.substring(start)
    Some(id)
  }
}
