package org.intellij.scala.processor

import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import org.intellij.scala.ZeppelinRunType
import org.jetbrains.plugins.scala.lang.psi.api.{FileDeclarationsHolder, ScalaFile}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings

/**
  * Default imports and builtins for the Spark interpreter
  */
object DeclarationUtil {
  private val DEFAULT_IMPORTS = Seq(
    "org.apache.spark.SparkContext._",
    "sqlContext.implicits._",
    "sqlContext.sql",
    "org.apache.spark.sql.functions._",
    "org.apache.spark.SparkContext._")

  private val DEFAULT_BUILTINS = Seq(
    ("sparkSession", "org.apache.spark.sql.SparkSession"),
    ("sqlContext", "org.apache.spark.sql.SQLContext"),
    ("z", "org.apache.zeppelin.spark.SparkZeppelinContext"),
    ("sc", "org.apache.spark.SparkContext"))

  def executeImplicitImportsDeclarations(processor: PsiScopeProcessor,
                                         file: FileDeclarationsHolder,
                                         state: ResolveState): Unit = {
    file match {
      case zeppelinFile: ScalaFile if isZeppelinFile(zeppelinFile) => {
        DEFAULT_BUILTINS.foreach {
          case (name, txt) => {
            ScalaPsiElementFactory
              .createElementFromText(s"class A { val $name: $txt = ??? }")(zeppelinFile.projectContext)
              .processDeclarations(processor, state, null, zeppelinFile)
          }
        }

        DEFAULT_IMPORTS.foreach {
          imp => {
            val importStmt = ScalaPsiElementFactory.createImportFromText(s"import $imp")(zeppelinFile.projectContext)
            importStmt.processDeclarations(processor, state, null, zeppelinFile)
          }
        }
      }
      case _ =>
    }
  }

  private def isZeppelinFile(zeppelinFile: FileDeclarationsHolder with ScalaFile): Boolean = {
    WorksheetFileSettings.getRunType(zeppelinFile) match {
      case _: ZeppelinRunType => true
      case _ => false
    }
  }
}
