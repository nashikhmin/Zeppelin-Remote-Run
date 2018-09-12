package org.intellij.plugin.zeppelin.scala.constants

import org.jetbrains.plugins.scala.worksheet.cell.CellManager

object ZeppelinScalaConstants {
  val ZEPPELIN_WORKSHEET_SETTINGS_TITLE = "Zeppelin Worksheet settings"

  val EXTERNAL_ID = "id="
  val PARAGRAPH_WITH_EXTERNAL_ID: String = CellManager.CELL_START_MARKUP+EXTERNAL_ID
  val TEMPLATE_TEXT: String =
    """//Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)
      |//So you don't need to create them manually
      |//
      |//NOTE: '//##' is a symbol of a start of a paragraph
      |//You can start right here!
      |
      |//##
      |val a = 1
      |val b = 2
      |
      |//##
      |a + b
      |    """.stripMargin
}