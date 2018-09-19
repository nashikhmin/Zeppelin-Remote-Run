package org.intellij.plugin.zeppelin.idea.settings.interpreter

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import org.intellij.plugin.zeppelin.components.ZeppelinComponent
import org.intellij.plugin.zeppelin.extensionpoints.UpdateInterpreterHandler
import org.intellij.plugin.zeppelin.models.Dependency
import org.intellij.plugin.zeppelin.models.InstantiationType
import org.intellij.plugin.zeppelin.models.Interpreter
import org.intellij.plugin.zeppelin.models.InterpreterOption
import org.intellij.plugin.zeppelin.service.ZeppelinActionService
import javax.swing.JComponent

open class InterpreterSettingsDialog(private val project: Project,
                                     var interpreter: Interpreter) : DialogWrapper(project) {

    override fun getTitle(): String = "${interpreter.name} interpreter settings"

    private val myPanel: InterpreterSettingsForm = InterpreterSettingsForm()
    override fun createCenterPanel(): JComponent = myPanel.contentPane
    override fun doOKAction() {
        val newDependencies: List<Dependency> = getNewDependencies()
        val newOptions: InterpreterOption = getNewOptions()
        interpreter = interpreter.copy(dependencies = newDependencies, option = newOptions)
        val connection: ZeppelinComponent = ZeppelinComponent.connectionFor(project)
        val actionService: ZeppelinActionService = connection.service
        actionService.updateInterpreterSettings(interpreter)
        UpdateInterpreterHandler.getAll().forEach { it.updateInterpreter(project) }
        super.doOKAction()
    }

    override fun init() {
        super.init()
        updateDependencyList()
        updateInstantiationType()
    }

    private fun updateDependencyList() {
        val dependencies = interpreter.dependencies.map {it.groupArtifactVersion}
        myPanel.initDataModel(dependencies)
    }

    private fun updateInstantiationType() {
        val options: InterpreterOption = interpreter.option
        val values: List<String> = InstantiationType.values().toList().map { it.toString() }
        myPanel.initInstantiationTypes(values, options)
    }

    private fun getNewDependencies(): List<Dependency> {
        val dependenciesNames: List<String?> = myPanel.modelList.toList()
        val originalDependencies: List<Dependency> = interpreter.dependencies
        return dependenciesNames.filter { it != null }
            .map { it -> originalDependencies.find { origIt -> origIt.groupArtifactVersion == it } ?: Dependency(it!!) }
    }

    private fun getNewOptions(): InterpreterOption {
        return if (myPanel.isGlobally) {
            InterpreterOption()
        } else {
            InterpreterOption(InstantiationType.valueOf(myPanel.perNoteValue),
                              InstantiationType.valueOf(myPanel.perUserValue))
        }
    }
}