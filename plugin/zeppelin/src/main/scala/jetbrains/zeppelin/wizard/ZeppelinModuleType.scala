package jetbrains.zeppelin.wizard


import com.intellij.ide.util.projectWizard.EmptyModuleBuilder
import com.intellij.openapi.module.{Module, ModuleType}
import javax.swing.Icon
import jetbrains.zeppelin.constants.ZeppelinConstants
import jetbrains.zeppelin.wizard.ZeppelinModuleType._

class ZeppelinModuleType extends ModuleType[EmptyModuleBuilder](Id) {
  def createModuleBuilder() = new EmptyModuleBuilder()

  override def getDescription: String = ZeppelinConstants.MODULE_DESCRIPTION

  override def getName: String = ZeppelinConstants.MODULE_NAME

  override def getNodeIcon(isOpened: Boolean): Icon = ZeppelinConstants.MODULE_ICON
}

object ZeppelinModuleType {
  val Id: String = ZeppelinConstants.MODULE_ID

  val instance: ZeppelinModuleType =
    Class.forName("jetbrains.zeppelin.wizard.ZeppelinModuleType").newInstance.asInstanceOf[ZeppelinModuleType]

  def unapply(m: Module): Option[Module] = {
    if (ModuleType.get(m).isInstanceOf[ZeppelinModuleType]) {
      Some(m)
    } else {
      None
    }
  }
}