package org.intellij.plugin.zeppelin.models

import com.squareup.moshi.Json

data class Config(val enabled: Boolean? = true)

enum class InterpreterStatus {
    READY, DOWNLOADING_DEPENDENCIES, ERROR, PENDING
}

data class InterpreterBindings(val id: String, val name: String, val interpreters: List<InterpreterBinding>)

data class InterpreterBinding(val defaultInterpreter: Boolean, val name: String)

enum class InstantiationType(val value: String) {
    @Json(name = "shared")
    SHARED("shared"),
    @Json(name = "scoped")
    SCOPED("scoped"),
    @Json(name = "isolated")
    ISOLATED("isolated");
}

data class InterpreterOption(val perNote: InstantiationType = InstantiationType.SHARED,
                             val perUser: InstantiationType = InstantiationType.SHARED) {
    fun isGlobally(): Boolean = perNote == InstantiationType.SHARED && perUser == InstantiationType.SHARED
}

data class Interpreter(val id: String,
                       val name: String,
                       val group: String,
                       val dependencies: List<Dependency>,
                       val status: InterpreterStatus,
                       val properties: Any,
                       val option: InterpreterOption,
                       val interpreterGroup: List<Any>,
                       val errorReason: String?)

data class Dependency(val groupArtifactVersion: String,
                      val exclusions: List<String> = emptyList(),
                      val local: Boolean = true)

data class Notebook(val id: String, val name: String = "", val paragraphs: List<Paragraph> = emptyList())

data class Paragraph(val id: String,
                     val jobName: String = "",
                     val status: String = "",
                     val user: String = "",
                     val config: Config = Config(),
                     val title: String = "",
                     val text: String = "")

data class Credentials(val principal: String, val ticket: String, val roles: String)

/**
 * Model for new notebook request by REST API
 */
data class NewNotebook(val name: String, val paragraphs: List<Paragraph> = listOf())

/**
 * The connection status to the server
 */
enum class ConnectionStatus {
    CONNECTED, FAILED, DISCONNECTED
}

/**
 * Login status
 */
enum class LoginStatus {
    LOGGED, NOT_LOGGED
}

data class User(val name: String, val password: String)