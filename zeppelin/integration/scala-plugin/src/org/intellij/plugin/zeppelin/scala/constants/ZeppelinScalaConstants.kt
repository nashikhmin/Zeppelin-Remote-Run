package org.intellij.plugin.zeppelin.scala.constants

object ZeppelinScalaConstants {
    const val ZEPPELIN_WORKSHEET_SETTINGS_TITLE: String = "Zeppelin Worksheet settings"
    const val EXTERNAL_ID: String = "id="
    const val PARAGRAPH_WITH_EXTERNAL_ID: String = "//##$EXTERNAL_ID"
    const val TEMPLATE_TEXT: String =
            """//Zeppelin creates and injects sc (SparkContext) and sqlContext (HiveContext or SqlContext)
//So you don't need to create them manually
//
//NOTE: '//##' is a symbol of a start of a paragraph
//You can start right here!

//##
val a = 1
val b = 2

//##
a + b"""
}