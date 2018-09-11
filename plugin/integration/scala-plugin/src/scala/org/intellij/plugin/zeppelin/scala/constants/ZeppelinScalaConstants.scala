package org.intellij.plugin.zeppelin.scala.constants

object ZeppelinScalaConstants {
  val ZEPPELIN_WORKSHEET_SETTINGS_TITLE = "Zeppelin Worksheet settings"

  val TEMPLATE_TEXT: String =
    """//##
      |val a = 1
      |val b = 2
      |
      |//##
      |a + b
      |"""
      .format().stripMargin
}