package org.intellij.plugin.zeppelin.idea.settings.plugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

@com.intellij.openapi.components.State(name = "RemoteRunSettings", storages = [Storage("remoteRunPlugin.xml")])
class RemoteRunApplicationSettings : PersistentStateComponent<RemoteRunSettingsState>,
                                     ZeppelinHolder {
    override fun getZeppelinSettings(): ZeppelinSettings = myState.zeppelinSettings.data

    override fun setZeppelinSettings(settings: ZeppelinSettings) {
        ApplicationManager.getApplication().messageBus.syncPublisher(SettingsChangedListener.TOPIC)
                .beforeSettingsChanged(this)
        myState.zeppelinSettings.data = settings
        ZeppelinCredentialsManager.setCredentials(settings.user)
    }

    override fun getState(): RemoteRunSettingsState? = myState

    var myState = RemoteRunSettingsState()


    override fun loadState(state: RemoteRunSettingsState): Unit = XmlSerializerUtil.copyBean(state, this.myState)


    companion object {
        fun getInstance(project: Project): RemoteRunApplicationSettings =
                ServiceManager.getService(project, RemoteRunApplicationSettings::class.java)
    }
}

interface SettingsChangedListener {
    companion object {
        val TOPIC: Topic<SettingsChangedListener> = Topic.create("ZeppelinSettingsChanged",
                                                                 SettingsChangedListener::class.java)
    }

    fun beforeSettingsChanged(settings: RemoteRunApplicationSettings)
}