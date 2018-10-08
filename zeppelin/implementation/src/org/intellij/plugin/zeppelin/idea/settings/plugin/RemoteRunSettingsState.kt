package org.intellij.plugin.zeppelin.idea.settings.plugin

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.models.User

data class RemoteRunSettingsState(
        @Property(surroundWithTag = false)
        var zeppelinSettings: ZeppelinSettingsStorage = ZeppelinSettingsStorage())

class ZeppelinSettingsStorage {

    private var login: String = ZeppelinCredentialsManager.getLogin()
    private var password: String = ZeppelinCredentialsManager.getPlainPassword()

    @Attribute("defaultName")
    var defaultNotebookName: String = ZeppelinConstants.DEFAULT_NOTEBOOK_NAME

    @Attribute("sparkVersion")
    var sparkVersion: String = SparkVersion.ZEPPELIN_DEFAULT_VERSION.toString()

    @Attribute("host")
    var host: String = "localhost"

    @Attribute("port")
    var port: Int = 8080

    @Attribute("IsAnonymous")
    var isAnonymous: Boolean = false

    private fun setCredentials(login: String, password: String) {
        this.login = login
        this.password = password
    }

    val user: User?
        get() {
            if (isAnonymous) return null
            val login: String = ZeppelinCredentialsManager.getLogin()
            val password: String = ZeppelinCredentialsManager.getPlainPassword()
            return User(login, password)
        }

    var data: ZeppelinSettings
        get() = ZeppelinSettings(host, port, user, SparkVersion(sparkVersion), defaultNotebookName)
        set(value) {
            host = value.host
            port = value.port
            sparkVersion = value.sparkVersion.toString()
            defaultNotebookName = value.defaultNotebookName
            if (value.user != null) {
                isAnonymous = false
                setCredentials(login = value.user.name, password = value.user.password)
            } else {
                isAnonymous = true
            }
        }
}

data class ZeppelinSettings(val host: String,
                            val port: Int,
                            val user: User?,
                            val sparkVersion: SparkVersion,
                            val defaultNotebookName: String) {
    val isAnonymous
        get() = user == null

    val fullUrl: String
        get() = "$host:$port"

    val login
        get() = user?.name ?: ""

    val password
        get() = user?.password ?: ""
}

interface ZeppelinHolder {
    fun getZeppelinSettings(): ZeppelinSettings
    fun setZeppelinSettings(settings: ZeppelinSettings)
}