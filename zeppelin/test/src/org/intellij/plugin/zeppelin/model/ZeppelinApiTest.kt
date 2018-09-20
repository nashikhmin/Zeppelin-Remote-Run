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

        val createdNotebook = integration.api.createNotebook("Test notebook")
        assertTrue(createdNotebook.id.isNotEmpty(),"The id of the created notebook is empty")
    }

    @Test
    fun interpretersTest() {
        val interpreters = integration.api.allInterpreters()
        assertTrue(interpreters.isNotEmpty(), "There are not 6 notebooks in Zeppelin")
    }
}
