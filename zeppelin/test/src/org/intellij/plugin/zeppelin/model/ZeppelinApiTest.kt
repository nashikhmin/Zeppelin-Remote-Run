package org.intellij.plugin.zeppelin.model

import org.intellij.plugin.zeppelin.api.ZeppelinIntegration
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZeppelinApiTest {
    private val settings = getMockSettings()
    private val integration = ZeppelinIntegration(settings, MockExecutionHandlerFactory())

    @Test
    fun loginTest() {
        assertTrue(integration.isConnected(), "Connection is failed")
    }

    @Test
    fun notebooksTest() {
        val notebooks = integration.api.allNotebooks().filter { it.name.startsWith("Zeppelin Tutorial") }
        assertEquals(notebooks.size, 6, "There are not 6 notebooks in Zeppelin")
        notebooks.forEach { assertTrue(it.paragraphs.isNotEmpty(), "There are not paragraphs in ${it.name}") }

        val notebookName = "Test notebook"
        val createdNotebook = integration.api.createNotebook(notebookName)
        assertTrue(createdNotebook.id.isNotEmpty(), "The id of the created notebook is empty")


        assertTrue(integration.api.notebooksByPrefix(notebookName).isNotEmpty(),
                "There are not created notebooks in Zeppelin")

        integration.api.deleteAllNotebooksByPrefix(notebookName)
        assertTrue(integration.api.notebooksByPrefix(notebookName).isEmpty(), "The notebooks are not deleted")
    }

    @Test
    fun interpretersTest() {
        val interpreters = integration.api.allInterpreters()
        assertTrue(interpreters.isNotEmpty(), "There are not interpreters in Zeppelin")
    }

    @Test
    fun interpreterTest() {
        val interpreterTestNotebook = "interpreter notebook"
        val createdNotebook = integration.api.createNotebook(interpreterTestNotebook)
        val interpreter = integration.api.defaultInterpreter(createdNotebook.id)
        assertTrue((interpreter.name == "spark"), "The default notebook is not spark")
        assertEquals(interpreter.name, integration.api.interpreterById(interpreter.id)?.name, "The interpreters is not the same")
    }
}