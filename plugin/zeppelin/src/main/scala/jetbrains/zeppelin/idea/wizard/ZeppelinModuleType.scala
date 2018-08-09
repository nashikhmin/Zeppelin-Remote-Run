package jetbrains.zeppelin.idea.wizard


import com.intellij.openapi.module.{Module, ModuleType}
import javax.swing.Icon
import jetbrains.zeppelin.constants.ZeppelinConstants
import jetbrains.zeppelin.idea.wizard.ZeppelinModuleType._

class ZeppelinModuleType extends ModuleType[ZeppelinModuleBuilder](Id) {
  def createModuleBuilder() = new ZeppelinModuleBuilder

  override def getDescription: String = ZeppelinConstants.MODULE_DESCRIPTION

  override def getName: String = ZeppelinConstants.MODULE_NAME

  override def getNodeIcon(isOpened: Boolean): Icon = ZeppelinConstants.MODULE_ICON
}

object ZeppelinModuleType {
  val Id: String = ZeppelinConstants.MODULE_ID

  val instance: ZeppelinModuleType =
    Class.forName("jetbrains.zeppelin.idea.wizard.ZeppelinModuleType").newInstance.asInstanceOf[ZeppelinModuleType]

  def unapply(m: Module): Option[Module] = {
    if (ModuleType.get(m).isInstanceOf[ZeppelinModuleType]) {
      Some(m)
    } else {
      None
    }
  }
}