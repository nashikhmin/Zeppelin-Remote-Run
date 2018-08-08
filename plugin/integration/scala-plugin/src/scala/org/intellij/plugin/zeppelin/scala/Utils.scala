package org.intellij.plugin.zeppelin.scala

import com.intellij.psi.PsiFile
import org.intellij.plugin.zeppelin.scala.runner.ZeppelinRunType
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings

/**
  * Common methods for Scala plugin
  */
object Utils {
  def isZeppelinWorksheet(originalFile: PsiFile): Boolean = {
    originalFile match {
      case file: PsiFile => {
        WorksheetFileSettings.getRunType(file) match {
          case _: ZeppelinRunType => true
          case _ => false
        }
      }
      case _ => false
    }
  }
}