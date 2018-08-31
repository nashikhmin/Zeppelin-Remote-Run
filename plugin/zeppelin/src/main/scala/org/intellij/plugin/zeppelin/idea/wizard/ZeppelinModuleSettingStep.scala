package org.intellij.plugin.zeppelin.idea.wizard

import java.util

import com.intellij.facet.impl.ui.libraries.{LibraryCompositionSettings, LibraryOptionsPanel}
import com.intellij.framework.library.FrameworkLibraryVersionFilter
import com.intellij.ide.util.projectWizard.{ModuleBuilder, ModuleWizardStep, SettingsStep}
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainerFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.panels.VerticalLayout
import javax.swing.{JComponent, JPanel}

class ZeppelinModuleSettingStep(moduleBuilder: ModuleBuilder, settingsStep: SettingsStep) extends ModuleWizardStep {
  private val basePath = moduleBuilder.getContentEntryPath
  private val myJavaStep = ZeppelinModuleType.getModuleType.modifyProjectTypeStep(settingsStep, moduleBuilder)
  private val project = settingsStep.getContext.getProject
  private val librariesContainer = LibrariesContainerFactory.createContainer(project)
  private val customLibraryDescription = ZeppelinLibraryDescription(project)

  private var libraryCompositionSettings: LibraryCompositionSettings = _
  private var libraryOptionsPanel: LibraryOptionsPanel = _
  private var panel: JPanel = _

  settingsStep.addSettingsComponent(getComponent)
  moduleBuilder.addModuleConfigurationUpdater(createModuleConfigurationUpdater)

  override def disposeUIResources(): Unit = {
    if (libraryOptionsPanel != null) Disposer.dispose(libraryOptionsPanel)
  }

  override def getComponent: JComponent = {
    if (panel == null) {
      panel = new JPanel(new VerticalLayout(0))
      panel.setBorder(IdeBorderFactory.createTitledBorder("Zeppelin version"))
      panel.add(getLibraryPanel.getMainPanel)
    }
    panel
  }

  override def updateDataModel(): Unit = {
    libraryCompositionSettings = getLibraryPanel.apply
    if (myJavaStep != null) myJavaStep.updateDataModel()
  }

  protected def createModuleConfigurationUpdater: ModuleBuilder.ModuleConfigurationUpdater = {
    (_: Module, rootModel: ModifiableRootModel) => {
      if (libraryCompositionSettings != null) {
        libraryCompositionSettings.addLibraries(rootModel, new util.ArrayList[Library](), librariesContainer)
      }
    }
  }

  private def getLibraryPanel = {
    if (libraryOptionsPanel == null) {
      val baseDirPath = if (basePath != null) FileUtil.toSystemIndependentName(basePath) else ""

      libraryOptionsPanel = new LibraryOptionsPanel(
        customLibraryDescription,
        baseDirPath,
        FrameworkLibraryVersionFilter.ALL,
        librariesContainer,
        false)
    }
    libraryOptionsPanel
  }
}