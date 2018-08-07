package org.intellij.scala

import com.intellij.psi._
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.worksheet.FileDeclarationsContributor
import org.jetbrains.plugins.scala.worksheet.settings.WorksheetFileSettings

class ZeppelinFileDeclarationContributor extends FileDeclarationsContributor {
  private val DEFAULT_IMPORTS: List[String] = List(
    "org.apache.spark.SparkContext._",
    "sqlContext.sql",
    "org.apache.spark.sql.functions._",
    "org.apache.spark.SparkContext._",
    "sqlContext.implicits._")

  private val DEFAULT_BUILTINS: List[(String, String)] = List(
    ("sparkSession", "org.apache.spark.sql.SparkSession"),
    ("sqlContext", "org.apache.spark.sql.SQLContext"),
    ("z", "org.apache.zeppelin.spark.SparkZeppelinContext"),
    ("sc", "org.apache.spark.SparkContext"))

  override def accept(holder: PsiElement): Boolean = {
    val originalFile = holder.getFirstChild.getContainingFile.getOriginalFile
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

  override def processAdditionalDeclarations(processor: PsiScopeProcessor,
                                             holder: PsiElement,
                                             state: ResolveState): Unit = {
    holder match {
      case zeppelinFile: ScalaFile => {
        DEFAULT_BUILTINS.foreach {
          case (name, txt) => {
            ScalaPsiElementFactory
              .createElementFromText(s"class A { val $name:$txt = null }")(zeppelinFile.projectContext)
              .processDeclarations(processor, state, null, zeppelinFile)
          }
        }

        DEFAULT_IMPORTS.foreach {
          imp => {
            val importStmt = ScalaPsiElementFactory.createImportFromText(s"import $imp")(zeppelinFile.projectContext)
            importStmt.processDeclarations(processor, state, null, zeppelinFile)
          }
        }
        importSqlContextImplicits(processor, holder, state, zeppelinFile)
      }
      case _ =>
    }
  }

  private def importSqlContextImplicits(processor: PsiScopeProcessor,
                                        holder: PsiElement,
                                        state: ResolveState,
                                        zeppelinFile: ScalaFile) = {
    //TODO: change scope to module
    val clazz = JavaPsiFacade.getInstance(holder.getProject)
      .findClass("org.apache.spark.sql.SQLContext", GlobalSearchScope.allScope(holder.getProject))
    clazz.getInnerClasses.toList.head.processDeclarations(processor, state, null, zeppelinFile)
  }
}