package org.intellij.plugin.zeppelin.idea.settings.plugin

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.models.SparkVersion
import org.intellij.plugin.zeppelin.models.User

data class RemoteRunSettingsState(@Property(surroundWithTag = false) var zeppelinSettings: ZeppelinSettings = ZeppelinSettings())

class ZeppelinSettings {

    var login: String = ZeppelinCredentialsManager.getLogin()
    var password: String = ZeppelinCredentialsManager.getPlainPassword()

  @Attribute("defaultName")
    var defaultNotebookName: String = ZeppelinConstants.DEFAULT_NOTEBOOK_NAME

    @Attribute("sparkVersion")
    var sparkVersion: String = SparkVersion.ZEPPELIN_DEFAULT_VERSION.toString()

    @Attribute("address")
    var address: String = "localhost"

    @Attribute("port")
    var port: Int = 8080

    @Attribute("IsAnonymous")
    var isAnonymous: Boolean = false


    val fullUrl: String
        get() = "$address:$port"

    fun setCredentials(login: String, password: String) {
        this.login = login
        this.password = password
    }


    fun setIsAnonymous(value: Boolean) {
        isAnonymous = value
    }


    fun user(): User? {
        if (isAnonymous) return null
        val login: String = ZeppelinCredentialsManager.getLogin()
        val password: String = ZeppelinCredentialsManager.getPlainPassword()
        return User(login, password)
    }
}

interface ZeppelinHolder {
    fun getZeppelinSettings(): ZeppelinSettings
    fun setZeppelinSettings(settings: ZeppelinSettings)
}