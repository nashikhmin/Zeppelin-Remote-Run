package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.startup.StartupManager
import org.intellij.plugin.zeppelin.extensionpoints.TemplateFileCreator

open class ZeppelinModuleBuilder : JavaModuleBuilder() {
    override fun createWizardSteps(wizardContext: WizardContext,
                                   modulesProvider: ModulesProvider): Array<out ModuleWizardStep>? {
        return ModuleWizardStep.EMPTY_ARRAY
    }

    override fun getModuleType(): ModuleType<*> = ZeppelinModuleType.getModuleType()
    override fun modifyProjectTypeStep(settingsStep: SettingsStep): ModuleWizardStep {
        return StdModuleTypes.JAVA.modifyProjectTypeStep(settingsStep, this)!!
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep {
        return ZeppelinModuleSettingStep(this, settingsStep)
    }

    override fun setupRootModel(rootModel: ModifiableRootModel) {
        super.setupRootModel(rootModel)
        val contentEntryPath: String = this.contentEntryPath ?: return
        val srcPath = "$contentEntryPath/src"
        val module: Module = rootModel.module
        val project: Project = module.project
        StartupManager.getInstance(project)
            .runWhenProjectIsInitialized { TemplateFileCreator.get()?.create(srcPath, project) }
    }
}