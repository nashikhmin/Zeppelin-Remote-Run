package org.intellij.plugin.zeppelin.test.api

import org.intellij.plugin.zeppelin.api.remote.ZeppelinIntegration
import org.junit.After
import org.junit.Before

open class AbstractApiTest {
    private val settings = getMockSettings()
    protected val integration = ZeppelinIntegration(settings,MockExecutionHandlerFactory())
    protected val api = integration.api

    protected val notebookTestName = "APITEST/Test notebook"

    @After
    @Before
    fun clean() {
        integration.connect()
        integration.api.deleteNotebooksByPrefix(notebookTestName)
    }
}