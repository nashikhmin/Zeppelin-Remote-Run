package org.intellij.plugin.zeppelin.test.api

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotebookTest : AbstractApiTest() {
    @Test
    fun notebookTest() {
        val notebook = api.createNotebook(notebookTestName)
        val paragraphText = "Example"
        api.createParagraph(notebook.id, paragraphText)
        val paragraphs = api.getNotebookById(notebook.id)?.paragraphs?: listOf()
        assertEquals(paragraphs.size, 1, "There is not one of paragraph in the notebook")
        assertEquals(paragraphs.last().text, paragraphText, "The paragraph test is not the same")
    }


    @Test
    fun defaultNotebooksListTest() {
        val notebooks = api.allNotebooks().filter { it.name.startsWith("Zeppelin Tutorial") }
        assertEquals(notebooks.size, 6, "There are not 6 notebooks in Zeppelin")
        notebooks.forEach { assertTrue(it.paragraphs.isNotEmpty(), "There are not paragraphs in ${it.name}") }
    }

    @Test
    fun noRemovedNotebooksInList() {
        api.createNotebook(notebookTestName)
        api.deleteNotebooksByPrefix(notebookTestName)
        val notebooks = api.allNotebooks().filter { it.name.contains("Trash") }
        assertTrue(notebooks.isEmpty(),"An all notebooks list contains deleted notebooks")
    }

    @Test
    fun createAndDeleteNotebookTest() {
        val createdNotebook = api.createNotebook(notebookTestName)
        assertTrue(createdNotebook.id.isNotEmpty(), "The id of the created notebook is empty")

        assertTrue(api.notebooksByPrefix(notebookTestName).isNotEmpty(),
                "There are not created notebooks in Zeppelin")

        api.deleteNotebooksByPrefix(notebookTestName)
        assertTrue(integration.api.notebooksByPrefix(notebookTestName).isEmpty(), "The notebooks are not deleted")
    }
}