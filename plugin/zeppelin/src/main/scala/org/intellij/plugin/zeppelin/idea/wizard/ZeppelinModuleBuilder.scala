package jetbrains.zeppelin.idea.wizard

import com.intellij.ide.util.projectWizard._
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ui.configuration.ModulesProvider


class ZeppelinModuleBuilder extends JavaModuleBuilder {
  override def createWizardSteps(wizardContext: WizardContext,
                                 modulesProvider: ModulesProvider): Array[ModuleWizardStep] = {
    ModuleWizardStep.EMPTY_ARRAY
  }

  override def getModuleType: ModuleType[_ <: ModuleBuilder] = ZeppelinModuleType.getModuleType

  override def modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep = {
    new ZeppelinModuleSettingStep(this, settingsStep)
  }
}