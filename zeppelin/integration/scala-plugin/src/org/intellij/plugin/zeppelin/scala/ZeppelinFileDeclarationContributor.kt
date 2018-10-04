package org.intellij.plugin.zeppelin.scala

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.scala.worksheet.settings.ZeppelinFileSettings
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.worksheet.FileDeclarationsContributor

/**
 * Add to IDEA special Zeppelin imports and resolve Zeppelin built-in variables (sc, sqlc, etc)
 */
class ZeppelinFileDeclarationContributor : FileDeclarationsContributor() {
    override fun accept(holder: PsiElement): Boolean {
        val originalFile: PsiFile = holder.firstChild.containingFile.originalFile
        return ZeppelinFileSettings.isZeppelinWorksheet(originalFile)
    }

    override fun processAdditionalDeclarations(processor: PsiScopeProcessor, holder: PsiElement,
                                               state: ResolveState) {
        if (holder is ScalaFile) {
            val version: SparkVersion = ZeppelinComponent.connectionFor(holder.project).sparkVersion
            proceedBuiltins(version, holder, processor, state)
            proceedGlobalImports(version, holder, processor, state)
            proceedContextImplicits(version, holder, processor, holder, state)
        }
    }

    private fun proceedBuiltins(version: SparkVersion,
                                zeppelinFile: ScalaFile,
                                processor: PsiScopeProcessor,
                                state: ResolveState) {
        val project: Project = zeppelinFile.project
        val builtins: List<PsiElement> = ZeppelinFileDeclarationContributor.getBuiltins(version, project)
        builtins.forEach { it -> it.processDeclarations(processor, state, null, zeppelinFile) }
    }

    private fun proceedContextImplicits(version: SparkVersion, zeppelinFile: ScalaFile, processor: PsiScopeProcessor,
                                        holder: PsiElement, state: ResolveState) {
        val contextImplicits: List<String> = if (version.isSpark2()) {
            listOf("org.apache.spark.sql.SparkSession")
        } else {
            listOf("org.apache.spark.sql.SQLContext")
        }
        contextImplicits.forEach { it ->
            val psiClass = JavaPsiFacade.getInstance(holder.project).findClass(it,
                    GlobalSearchScope.allScope(holder.project)) ?: return
            val list: List<PsiClass> = psiClass.innerClasses.toList()
            list.first().processDeclarations(processor, state, null, zeppelinFile)
        }
    }

    private fun proceedGlobalImports(version: SparkVersion,
                                     zeppelinFile: ScalaFile,
                                     processor: PsiScopeProcessor,
                                     state: ResolveState) {
        val globalImports: List<PsiElement> = ZeppelinFileDeclarationContributor.getGlobalImports(version,
                zeppelinFile.project)
        globalImports.forEach { it -> it.processDeclarations(processor, state, null, zeppelinFile) }
    }

    companion object {
        private val DEFAULT_GLOBAL_IMPORTS: List<String> = listOf("org.apache.spark.SparkContext._")
        private val FUNCTIONS_IMPORTS: List<String> = listOf("org.apache.spark.sql.functions._")
        private val DEFAULT_BUILTINS: List<Pair<String, String>> = listOf(
                Pair("z", "org.apache.zeppelin.spark.SparkZeppelinContext"),
                Pair("sc", "org.apache.spark.SparkContext"),
                Pair("sqlContext", "org.apache.spark.sql.SQLContext"),
                Pair("sqlc", "org.apache.spark.sql.SQLContext"))
        private val SPARK_2_BULTINS: List<Pair<String, String>> = listOf(
                Pair("spark", "org.apache.spark.sql.SparkSession"))
        private var builtinsMap: Map<Pair<String, String>, List<PsiElement>> = mapOf()
        private var globalImportMap: Map<Pair<String, String>, List<PsiElement>> = mapOf()

        private fun getBuiltins(version: SparkVersion, project: Project): List<PsiElement> {
            val builtin = builtinsMap[Pair(version.versionString, project.name)]
            if (builtin != null) return builtin
            val computedBuiltins: List<PsiElement> = getInnerBuiltins(version, project)
            builtinsMap = mapOf(Pair(version.versionString, project.name) to computedBuiltins)
            return computedBuiltins
        }

        private fun getGlobalImports(version: SparkVersion, project: Project): List<PsiElement> {
            val imports: List<PsiElement>? = globalImportMap[Pair(version.versionString, project.name)]
            if (imports != null) return imports
            val computedGlobalImports: List<PsiElement> = getInnerGlobalImports(version, project)
            globalImportMap = mapOf(Pair(version.versionString, project.name) to computedGlobalImports)
            return computedGlobalImports
        }

        private fun getInnerBuiltins(version: SparkVersion, project: Project): List<PsiElement> {
            val builtinsNames = if (version.isSpark2()) {
                DEFAULT_BUILTINS + SPARK_2_BULTINS
            } else {
                DEFAULT_BUILTINS
            }
            return builtinsNames.map { it ->
                val name: String = it.first
                val txt: String = it.second
                ScalaPsiElementFactory.createElementFromText(
                        "class A { val $name:$txt = ??? }", project)
            }
        }

        private fun getInnerGlobalImports(version: SparkVersion, project: Project): List<PsiElement> {
            val globalImports: List<String> = if (!version.oldSqlContextImplicits()) {
                DEFAULT_GLOBAL_IMPORTS + FUNCTIONS_IMPORTS
            } else {
                DEFAULT_GLOBAL_IMPORTS
            }
            return globalImports.map { it ->
                ScalaPsiElementFactory.createImportFromText("import $it", project)
            }
        }
    }
}