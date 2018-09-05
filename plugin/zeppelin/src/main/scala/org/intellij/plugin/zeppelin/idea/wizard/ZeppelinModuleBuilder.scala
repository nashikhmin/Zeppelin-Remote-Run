package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.ide.util.projectWizard._
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.vfs.LocalFileSystem
import org.intellij.plugin.zeppelin.extensionpoints.TemplateFileCreator

class ZeppelinModuleBuilder extends JavaModuleBuilder {
  override def createWizardSteps(wizardContext: WizardContext,
                                 modulesProvider: ModulesProvider): Array[ModuleWizardStep] = {
    ModuleWizardStep.EMPTY_ARRAY
  }

  override def getModuleType: ModuleType[_ <: ModuleBuilder] = ZeppelinModuleType.getModuleType

  import com.intellij.ide.util.projectWizard.{ModuleWizardStep, SettingsStep}
  import com.intellij.openapi.module.StdModuleTypes

  override def modifyProjectTypeStep(settingsStep: SettingsStep): ModuleWizardStep = {
    StdModuleTypes.JAVA.modifyProjectTypeStep(settingsStep, this)
  }

  override def modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep = {
    new ZeppelinModuleSettingStep(this, settingsStep)
  }

  override def setupRootModel(rootModel: ModifiableRootModel): Unit = {
    super
      .setupRootModel(rootModel)
    val contentEntryPath = getContentEntryPath
    if (contentEntryPath == null) return

    val srcPath = contentEntryPath + "/src"
    val contentRoot = LocalFileSystem.getInstance().findFileByPath(srcPath)
    if (contentRoot == null) return


    val module = rootModel.getModule
    val project = module.getProject
    StartupManager.getInstance(project).runWhenProjectIsInitialized(() => {
      TemplateFileCreator.getAll.foreach(it => it.create(srcPath, project))
    })
  }
}