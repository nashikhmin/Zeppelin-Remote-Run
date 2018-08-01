package jetbrains.zeppelin.wizard

import com.intellij.icons.AllIcons
import com.intellij.platform.ProjectTemplate
import javax.swing.Icon

class ZeppelinProjectTemplate() extends ProjectTemplate {
  def getName: String = "Zeppelin Spark"

  def getDescription: String = "Module with a Zeppelin Spark"

  def getIcon: Icon = AllIcons.Nodes.Module

  def createModuleBuilder() = new ZeppelinModuleBuilder()

  def validateSettings(): Null = null
}