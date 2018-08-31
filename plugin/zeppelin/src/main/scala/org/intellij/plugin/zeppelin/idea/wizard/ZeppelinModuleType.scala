package org.intellij.plugin.zeppelin.idea.wizard


import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.module.{JavaModuleType, ModuleType, ModuleTypeManager}
import javax.swing.Icon
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.idea.wizard.ZeppelinModuleType._

class ZeppelinModuleType extends JavaModuleType(Id) {
  override def getDescription: String = ZeppelinConstants.MODULE_DESCRIPTION

  override def getName: String = ZeppelinConstants.MODULE_NAME

  override def getNodeIcon(isOpened: Boolean): Icon = ZeppelinConstants.MODULE_ICON
}

object ZeppelinModuleType {
  val Id: String = ZeppelinConstants.MODULE_ID

  def getModuleType: ModuleType[_ <: ModuleBuilder] = ModuleTypeManager.getInstance.findByID(Id)

  val instance: ZeppelinModuleType =
    Class.forName("org.intellij.plugin.zeppelin.idea.wizard.ZeppelinModuleType").newInstance
      .asInstanceOf[ZeppelinModuleType]
}