package org.intellij.plugin.zeppelin.api.rest

import org.intellij.plugin.zeppelin.models.ZeppelinException

data class RestApiException(val customMessage: String, val code: Int) : ZeppelinException() {
    override val message: String
        get() = "$customMessage.\nError code: $code"
}