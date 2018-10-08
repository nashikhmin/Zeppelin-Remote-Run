package org.intellij.plugin.zeppelin.test.api

import org.intellij.plugin.zeppelin.models.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterpreterTest: AbstractApiTest() {
    @Test
    fun assertInterpretersListTest() {
        val interpreters = api.allInterpreters()
        assertTrue(interpreters.isNotEmpty(), "There are not interpreters in Zeppelin")
    }

    @Test
    fun assertInterpreterInstantiationTypeTest() {
        val createdNotebook = api.createNotebook(notebookTestName)
        val interpreter = api.defaultInterpreter(createdNotebook.id)

        val newOption = InterpreterOption(InstantiationType.ISOLATED, InstantiationType.ISOLATED)
        api.updateInterpreterSetting(interpreter.copy(option = newOption))
        val gottenNewSettingsInterpreter = api.interpreterById(interpreter.id)
        assertEquals(newOption, gottenNewSettingsInterpreter?.option, "The interpreter settings have not changed")
        api.updateInterpreterSetting(interpreter)
    }

    @Test
    fun assertRestartInterpreterTest() {
        val createdNotebook = api.createNotebook(notebookTestName)
        val interpreter = api.defaultInterpreter(createdNotebook.id)
        api.restartInterpreter(interpreter.id, createdNotebook.id)
        val restartedInterpreter = api.interpreterById(interpreter.id)
        assertEquals(InterpreterStatus.READY, restartedInterpreter?.status, "The interpreter is not ready")
    }

    @Test
    fun assertNewDefaultInterpreterTest() {
        val createdNotebook = api.createNotebook(notebookTestName)
        val interpreter = api.defaultInterpreter(createdNotebook.id)

        val defaultInterpreter = "spark"
        assertTrue((interpreter.id == defaultInterpreter), "The default notebook is not spark")
        assertEquals(interpreter.name, api.interpreterById(interpreter.id)?.name,
                "The interpreters is not the same")

        val newDefaultInterpreter = "md"
        api.setDefaultInterpreter(createdNotebook.id, newDefaultInterpreter)
        assertEquals(newDefaultInterpreter, api.defaultInterpreter(createdNotebook.id).id,
                "The new default interpreter is not correct")
    }

    @Test
    fun assertAddMvnDependencyTest() {
        val createdNotebook = api.createNotebook(notebookTestName)
        val interpreter = api.defaultInterpreter(createdNotebook.id)

        val newDependencies = listOf(Dependency("junit:junit:4.12"))
        api.updateInterpreterSetting(interpreter.copy(dependencies = newDependencies))
        assertEquals(newDependencies,api.interpreterById(interpreter.id)?.dependencies, "Maven dependency has not been added")
        api.updateInterpreterSetting(interpreter)
    }

    @Test
    fun assertLocalFileDependencyTest() {
        val createdNotebook = api.createNotebook(notebookTestName)
        val interpreter = api.defaultInterpreter(createdNotebook.id)

        val newDependencies = listOf(Dependency("scala_example.jar"))
        api.updateInterpreterSetting(interpreter.copy(dependencies = newDependencies))
        assertEquals(newDependencies,api.interpreterById(interpreter.id)?.dependencies, "Local file dependency has not been added")
        api.updateInterpreterSetting(interpreter)
    }
}