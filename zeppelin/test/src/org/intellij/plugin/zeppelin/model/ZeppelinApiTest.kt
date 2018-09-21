package org.intellij.plugin.zeppelin.model

import org.intellij.plugin.zeppelin.api.ZeppelinIntegration
import org.intellij.plugin.zeppelin.models.InterpreterStatus
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test for validation of Zeppelin API
 */
class ZeppelinApiTest {
    private val settings = getMockSettings()
    private val integration = ZeppelinIntegration(settings, MockExecutionHandlerFactory())
    private val interpreterTestNotebook = "interpreter notebook"
    private val notebookTestName = "Test notebook"

    @After
    @Before
    fun clean() {
        integration.api.deleteNotebooksByPrefix(interpreterTestNotebook)
        integration.api.deleteNotebooksByPrefix(notebookTestName)
    }

    @Test
    fun loginTest() {
        assertTrue(integration.isConnected(), "Connection is failed")
    }

    @Test
    fun notebooksTest() {
        val notebooks = integration.api.allNotebooks().filter { it.name.startsWith("Zeppelin Tutorial") }
        assertEquals(notebooks.size, 6, "There are not 6 notebooks in Zeppelin")
        notebooks.forEach { assertTrue(it.paragraphs.isNotEmpty(), "There are not paragraphs in ${it.name}") }

        val createdNotebook = integration.api.createNotebook(notebookTestName)
        assertTrue(createdNotebook.id.isNotEmpty(), "The id of the created notebook is empty")


        assertTrue(integration.api.notebooksByPrefix(notebookTestName).isNotEmpty(),
                "There are not created notebooks in Zeppelin")

        integration.api.deleteNotebooksByPrefix(notebookTestName)
        assertTrue(integration.api.notebooksByPrefix(notebookTestName).isEmpty(), "The notebooks are not deleted")
    }

    @Test
    fun interpretersTest() {
        val interpreters = integration.api.allInterpreters()
        assertTrue(interpreters.isNotEmpty(), "There are not interpreters in Zeppelin")
    }

    @Test
    fun interpreterTest() {
        val createdNotebook = integration.api.createNotebook(interpreterTestNotebook)
        val interpreter = integration.api.defaultInterpreter(createdNotebook.id)
        assertTrue((interpreter.name == "spark"), "The default notebook is not spark")
        assertEquals(interpreter.name, integration.api.interpreterById(interpreter.id)?.name,
                "The interpreters is not the same")

        integration.api.restartInterpreter(interpreter.id, createdNotebook.id)
        val restartedInterpreter = integration.api.interpreterById(interpreter.id)
        assertEquals(InterpreterStatus.READY, restartedInterpreter?.status, "The interpreter is not ready")
    }
}