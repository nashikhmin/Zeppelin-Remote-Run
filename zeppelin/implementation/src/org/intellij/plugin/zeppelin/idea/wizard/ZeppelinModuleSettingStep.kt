package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.facet.impl.ui.libraries.LibraryCompositionSettings
import com.intellij.facet.impl.ui.libraries.LibraryOptionsPanel
import com.intellij.framework.library.FrameworkLibraryVersionFilter
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleBuilder.ModuleConfigurationUpdater
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainerFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.panels.VerticalLayout
import javax.swing.JComponent
import javax.swing.JPanel

open class ZeppelinModuleSettingStep(moduleBuilder: ModuleBuilder, settingsStep: SettingsStep) : ModuleWizardStep() {
    private val basePath: String? = moduleBuilder.contentEntryPath
    private val myJavaStep: ModuleWizardStep? = ZeppelinModuleType.getModuleType()
        .modifyProjectTypeStep(settingsStep, moduleBuilder)
    private val project: Project? = settingsStep.context.project

    private val librariesContainer: LibrariesContainer = LibrariesContainerFactory.createContainer(project)
    private val customLibraryDescription = ZeppelinLibraryDescription()

    private var libraryCompositionSettings: LibraryCompositionSettings? = null
    private val libraryOptionsPanel: LibraryOptionsPanel = createLibraryPanel()
    private var panel: JPanel = createPanel()

    init {
        settingsStep.addSettingsComponent(component)
        moduleBuilder.addModuleConfigurationUpdater(createModuleConfigurationUpdater())
    }
    override fun disposeUIResources() {
        Disposer.dispose(libraryOptionsPanel)
    }

    private fun createPanel(): JPanel {
        val jPanel = JPanel(VerticalLayout(0))
        jPanel.border = IdeBorderFactory.createTitledBorder("Zeppelin settings")
        jPanel.add(libraryOptionsPanel.mainPanel)
        return jPanel
    }

    override fun getComponent(): JComponent = panel

    override fun updateDataModel() {
        libraryCompositionSettings = libraryOptionsPanel.apply()
        myJavaStep?.updateDataModel()
    }

    protected fun createModuleConfigurationUpdater(): ModuleConfigurationUpdater = object : ModuleConfigurationUpdater() {
        override fun update(module: Module, rootModel: ModifiableRootModel) {
            libraryCompositionSettings?.addLibraries(rootModel, ArrayList<Library>(), librariesContainer)
        }
    }


    private fun createLibraryPanel(): LibraryOptionsPanel {
        val baseDirPath: String = basePath?.let { FileUtil.toSystemIndependentName(it) } ?: ""
        return LibraryOptionsPanel(customLibraryDescription,
                                   baseDirPath,
                                   FrameworkLibraryVersionFilter.ALL,
                                   librariesContainer,
                                   false)
    }
}