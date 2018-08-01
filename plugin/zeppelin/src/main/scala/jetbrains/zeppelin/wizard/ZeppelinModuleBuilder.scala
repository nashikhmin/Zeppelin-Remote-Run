package jetbrains.zeppelin.wizard

import com.intellij.ide.util.projectWizard.{JavaModuleBuilder, ModuleWizardStep, SettingsStep, WizardContext}
import com.intellij.openapi.roots.ui.configuration.ModulesProvider


class ZeppelinModuleBuilder extends JavaModuleBuilder {
  override def createWizardSteps(wizardContext: WizardContext,
                                 modulesProvider: ModulesProvider): Array[ModuleWizardStep] = {
    ModuleWizardStep.EMPTY_ARRAY
  }

  override def modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep = {
    new ZeppelinModuleSettingStep(this, settingsStep)
  }
}