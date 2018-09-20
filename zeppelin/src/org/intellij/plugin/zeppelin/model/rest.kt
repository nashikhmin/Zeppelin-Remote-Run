package org.intellij.plugin.zeppelin.model

import com.squareup.moshi.Json
import org.intellij.plugin.zeppelin.models.Credentials

data class RestResponseMessage(val status: String, val message: String, val body: Any)
