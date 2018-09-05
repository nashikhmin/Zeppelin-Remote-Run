package org.intellij.plugin.zeppelin.scala.constants

object ZeppelinScalaConstants {
  val ZEPPELIN_WORKSHEET_SETTINGS_TITLE = "Zeppelin Worksheet settings"

  val TEMPLATE_TEXT: String =
    """
      |/**
      |Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)
      |So you don't need to create them manually
      |
      |NOTE: '//##' is a symbol of a start of a paragraph
      |*/
      |//Start write right here!
      |//##
      |val a = 1
      |val b = 2
      |
      |//##
      |a + b
    """.stripMargin
}