package org.intellij.plugin.zeppelin.model

data class RestResponseMessage(val status: String, val message: String, val body: Any = Any())
