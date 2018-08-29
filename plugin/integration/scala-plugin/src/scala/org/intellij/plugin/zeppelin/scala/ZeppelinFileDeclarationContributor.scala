package org.intellij.plugin.zeppelin.scala

import com.intellij.openapi.project.Project
import com.intellij.psi._
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinWorksheetFileSettings
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.worksheet.FileDeclarationsContributor

import scala.collection._

/**
  * Add to IDEA special Zeppelin imports and resolve Zeppelin built-in variables (sc, sqlc, etc)
  */
class ZeppelinFileDeclarationContributor extends FileDeclarationsContributor {
  override def accept(holder: PsiElement): Boolean = {
    val originalFile = holder.getFirstChild.getContainingFile.getOriginalFile
    ZeppelinWorksheetFileSettings.isZeppelinWorksheet(originalFile)
  }

  override def processAdditionalDeclarations(processor: PsiScopeProcessor,
                                             holder: PsiElement,
                                             state: ResolveState): Unit = {
    holder match {
      case zeppelinFile: ScalaFile => {
        val version = ZeppelinComponent.connectionFor(holder.getProject).sparkVersion

        proceedBuiltins(version, zeppelinFile, processor, holder, state)
        proceedGlobalImports(version, zeppelinFile, processor, holder, state)
        proceedContextImplicits(version, zeppelinFile, processor, holder, state)
      }
      case _ =>
    }
  }

  private def proceedBuiltins(version: SparkVersion,
                              zeppelinFile: ScalaFile,
                              processor: PsiScopeProcessor,
                              holder: PsiElement,
                              state: ResolveState): Unit = {

    val project = zeppelinFile.getProject
    val builtins: List[PsiElement] = ZeppelinFileDeclarationContributor.getBuiltins(version, project)
    builtins.foreach(it => it.processDeclarations(processor, state, null, zeppelinFile))
  }


  private def proceedContextImplicits(version: SparkVersion,
                                      zeppelinFile: ScalaFile,
                                      processor: PsiScopeProcessor,
                                      holder: PsiElement,
                                      state: ResolveState): Unit = {
    val contextImplicits = if (version.isSpark2) {
      List("org.apache.spark.sql.SparkSession")
    } else {
      List("org.apache.spark.sql.SQLContext")
    }

    contextImplicits.foreach(it => {
      val clazz = JavaPsiFacade.getInstance(holder.getProject)
        .findClass(it, GlobalSearchScope.allScope(holder.getProject))
      if (clazz == null) return
      val list = clazz.getInnerClasses.toList
      list.head.processDeclarations(processor, state, null, zeppelinFile)
      //val fields = clazz.getMethods.filter(_.getName=="sql").head
      //fields.processDeclarations(processor, state, null, zeppelinFile)
    })
  }

  private def proceedGlobalImports(version: SparkVersion,
                                   zeppelinFile: ScalaFile,
                                   processor: PsiScopeProcessor,
                                   holder: PsiElement,
                                   state: ResolveState): Unit = {
    val globalImports = ZeppelinFileDeclarationContributor.getGlobalImports(version, zeppelinFile.getProject)
    globalImports.foreach(it => it.processDeclarations(processor, state, null, zeppelinFile))
  }
}

object ZeppelinFileDeclarationContributor {
  private val DEFAULT_GLOBAL_IMPORTS: List[String] = List(
    "org.apache.spark.SparkContext._",
  )
  private val FUNCTIONS_IMPORTS: List[String] = List(
    "org.apache.spark.sql.functions._"
  )
  private val DEFAULT_BUILTINS: List[(String, String)] = List(
    ("z", "org.apache.zeppelin.spark.SparkZeppelinContext"),
    ("sc", "org.apache.spark.SparkContext"),
    ("sqlContext", "org.apache.spark.sql.SQLContext"),
    ("sqlc", "org.apache.spark.sql.SQLContext"),
  )
  private val SPARK_2_BULTINS: List[(String, String)] = List(
    ("spark", "org.apache.spark.sql.SparkSession"),
  )
  private var builtinsMap: Map[(String, String), List[PsiElement]] = Map()
  private var globalImportMap: Map[(String, String), List[PsiElement]] = Map()

  private def getBuiltins(version: SparkVersion, project: Project): List[PsiElement] = {
    val builtin = builtinsMap.get((version.versionString, project.getName))
    if (builtin.nonEmpty) return builtin.get
    val computedBuiltins = getInnerBuiltins(version, project)
    builtinsMap = Map((version.versionString, project.getName) -> computedBuiltins)
    computedBuiltins
  }

  private def getGlobalImports(version: SparkVersion, project: Project): List[PsiElement] = {
    val imports = globalImportMap.get((version.versionString, project.getName))
    if (imports.nonEmpty) return imports.get
    val computedGlobalImports = getInnerGlobalImports(version, project)
    globalImportMap = Map((version.versionString, project.getName) -> computedGlobalImports)
    computedGlobalImports
  }

  private def getInnerBuiltins(version: SparkVersion, project: Project): List[PsiElement] = {
    val builtinsNames = if (version.isSpark2) {
      DEFAULT_BUILTINS ++ SPARK_2_BULTINS
    }
    else {
      DEFAULT_BUILTINS
    }
    val builtins = builtinsNames.map(it => {
      val name = it._1
      val txt = it._2
      ScalaPsiElementFactory.createElementFromText(s"class A { val $name:$txt = ??? }")(project)
    })
    builtins
  }

  private def getInnerGlobalImports(version: SparkVersion, project: Project): List[PsiElement] = {
    val GLOBAL_IMPORTS = if (!version.oldSqlContextImplicits) {
      DEFAULT_GLOBAL_IMPORTS ++ FUNCTIONS_IMPORTS
    } else {
      DEFAULT_GLOBAL_IMPORTS
    }
    GLOBAL_IMPORTS.map(it => {
      ScalaPsiElementFactory.createImportFromText(s"import $it")(project)
    })
  }
}