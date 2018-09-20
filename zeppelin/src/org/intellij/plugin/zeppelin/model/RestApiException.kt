package org.intellij.plugin.zeppelin.model

import org.intellij.plugin.zeppelin.models.ZeppelinException

data class RestApiException(val customMessage: String, val error: String) : ZeppelinException() {
    override val message: String
        get() = "$customMessage.\nError: $error"
}