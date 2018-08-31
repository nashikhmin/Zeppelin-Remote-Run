package org.intellij.plugin.zeppelin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.{PersistentStateComponent, ServiceManager, State, Storage}
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil
import org.intellij.plugin.zeppelin.idea.settings.plugin.{RemoteRunSettingsState, ZeppelinCredentialsManager, ZeppelinHolder, ZeppelinSettings}

@State(
  name = "RemoteRunSettings",
  storages = Array(new Storage("remoteRunPlugin.xml"))
)
class RemoteRunApplicationSettings(project: Project) extends PersistentStateComponent[RemoteRunSettingsState] with
  ZeppelinHolder {
  var state = RemoteRunSettingsState()

  trait SettingsChangedListener {
    def beforeSettingsChanged(settings: RemoteRunApplicationSettings): Unit = {}

    def settingsChanged(settings: RemoteRunApplicationSettings): Unit = {}
  }

  object SettingsChangedListener {
    val TOPIC: Topic[SettingsChangedListener] = Topic
      .create("ZeppelinSettingsChanged", classOf[SettingsChangedListener])
  }

  override def getState: RemoteRunSettingsState = state

  override def getZeppelinSettings: ZeppelinSettings = state.zeppelinSettings

  override def loadState(state: RemoteRunSettingsState): Unit = XmlSerializerUtil.copyBean(state, this.state)

  override def setZeppelinSettings(settings: ZeppelinSettings): Unit = {
    ApplicationManager.getApplication.getMessageBus.syncPublisher(SettingsChangedListener.TOPIC)
      .beforeSettingsChanged(this)
    state.zeppelinSettings = settings
    ZeppelinCredentialsManager.setCredentials(settings.login, settings.password)
  }
}

object RemoteRunApplicationSettings {
  def getInstance(project: Project): RemoteRunApplicationSettings = {
    ServiceManager
      .getService(project, classOf[RemoteRunApplicationSettings])
  }
}