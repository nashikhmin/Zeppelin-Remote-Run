package jetbrains.zeppelin.idea.wizard

import com.intellij.platform.ProjectTemplate
import javax.swing.Icon
import jetbrains.zeppelin.constants.ZeppelinConstants

class ZeppelinProjectTemplate() extends ProjectTemplate {
  def createModuleBuilder() = new ZeppelinModuleBuilder()

  def getDescription: String = ZeppelinConstants.MODULE_DESCRIPTION

  def getIcon: Icon = ZeppelinConstants.MODULE_ICON

  def getName: String = ZeppelinConstants.MODULE_NAME

  def validateSettings(): Null = null
}