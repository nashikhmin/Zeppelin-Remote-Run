package org.intellij.plugin.zeppelin.idea.settings.plugin

import com.intellij.util.xmlb.annotations.{Attribute, Property}
import org.intellij.plugin.zeppelin.constants.ZeppelinConstants
import org.intellij.plugin.zeppelin.models.{SparkVersion, User}

case class RemoteRunSettingsState() {
  @Property(surroundWithTag = false) var zeppelinSettings: ZeppelinSettings = ZeppelinSettings()
}

//noinspection ScalaUnusedSymbol
class ZeppelinSettings {
  var login: String = ZeppelinCredentialsManager.getLogin
  var password: String = ZeppelinCredentialsManager.getPlainPassword

  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("defaultName")
  var defaultNotebookName: String = ZeppelinConstants.DEFAULT_NOTEBOOK_NAME

  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("sparkVersion")
  var sparkVersion: String = SparkVersion.ZEPPELIN_DEFAULT_VERSION.toString

  @SuppressWarnings(Array("Address"))
  @Attribute("UriEnabled")
  var address: String = "localhost"
  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("Port")
  var port: Int = 8080
  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("IsAnonymous")
  var isAnonymous: Boolean = false

  def fullUrl: String = s"$address:$port"

  def setAddress(value: String): Unit = {
    address = value
  }


  def setCredentials(login: String, password: String): Unit = {
    this.login = login
    this.password = password
  }

  def setDefaultNotebookName(value: String): Unit = {
    defaultNotebookName = value
  }

  def setIsAnonymous(value: Boolean): Unit = {
    isAnonymous = value
  }

  def setPort(value: Int): Unit = {
    port = value
  }

  def setSparkVersion(value: String): Unit = {
    sparkVersion = value
  }

  def user: Option[User] = {
    if (isAnonymous) return None
    val login = ZeppelinCredentialsManager.getLogin
    val password = ZeppelinCredentialsManager.getPlainPassword
    Some(User(login, password))
  }
}

object ZeppelinSettings {
  def apply(): ZeppelinSettings = new ZeppelinSettings()
}

trait ZeppelinHolder {
  def getZeppelinSettings: ZeppelinSettings

  def setZeppelinSettings(settings: ZeppelinSettings): Unit
}
