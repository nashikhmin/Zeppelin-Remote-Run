package org.intellij.plugin.zeppelin.model

import org.intellij.plugin.zeppelin.api.remote.ZeppelinIntegration
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
    private val integration = ZeppelinIntegration(settings,
            MockExecutionHandlerFactory())
    private val interpreterTestNotebook = "interpreter notebook"
    private val notebookTestName = "Test notebook"

    @After
    @Before
    fun clean() {
        integration.connect()
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
    fun notebookTest() {
        val notebook = integration.api.createNotebook(notebookTestName)
        val paragraphText = "Example"
        integration.api.createParagraph(notebook.id, paragraphText)
        val paragraphs = integration.api.getNotebookById(notebook.id)?.paragraphs?: listOf()
        assertEquals(paragraphs.size, 1, "There is not one of paragraph in the notebook")
        assertEquals(paragraphs.last().text, paragraphText, "The paragraph test is not the same")
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
        val defaultInterpreter = "spark"
        assertTrue((interpreter.id == defaultInterpreter), "The default notebook is not spark")
        assertEquals(interpreter.name, integration.api.interpreterById(interpreter.id)?.name,
                "The interpreters is not the same")

        val newDefaultInterpreter = "md"
        integration.api.setDefaultInterpreter(createdNotebook.id, newDefaultInterpreter)
        assertEquals(newDefaultInterpreter, integration.api.defaultInterpreter(createdNotebook.id).id,
                "The new default interpreter is not correct")

        integration.api.restartInterpreter(interpreter.id, createdNotebook.id)
        val restartedInterpreter = integration.api.interpreterById(interpreter.id)
        assertEquals(InterpreterStatus.READY, restartedInterpreter?.status, "The interpreter is not ready")
    }
}