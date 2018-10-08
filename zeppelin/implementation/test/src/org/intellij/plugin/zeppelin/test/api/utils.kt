package org.intellij.plugin.zeppelin.test.api

import org.intellij.plugin.zeppelin.idea.settings.plugin.ZeppelinSettings
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.models.User
import org.intellij.plugin.zeppelin.service.execution.ExecutionHandler
import org.intellij.plugin.zeppelin.service.execution.ExecutionHandlerFactory
import org.intellij.plugin.zeppelin.service.execution.ExecutionResults
import org.intellij.plugin.zeppelin.service.execution.OutputResponse

fun getMockSettings(): ZeppelinSettings = ZeppelinSettings(host = "localhost",
        port = 8080,
        sparkVersion = SparkVersion.ZEPPELIN_DEFAULT_VERSION,
        user = User("admin", "password1"),
        defaultNotebookName = "testNotebook")

class MockExecutionHandlerFactory : ExecutionHandlerFactory {
    override fun create(): ExecutionHandler = MockExecutionHandler()
}

class MockExecutionHandler : ExecutionHandler {
    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(msg: ExecutionResults) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onOutput(data: OutputResponse, isAppend: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProgress(percentage: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSuccess(msg: ExecutionResults) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onUpdateExecutionStatus(status: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}