package jetbrains.zeppelin.idea.settings.plugin

import com.intellij.util.xmlb.annotations.{Attribute, Property}
import jetbrains.zeppelin.constants.ZeppelinConstants
import jetbrains.zeppelin.models.{SparkVersion, User}

case class RemoteRunSettingsState() {
  @Property(surroundWithTag = false) var zeppelinSettings: ZeppelinSettings = ZeppelinSettings()
}

class ZeppelinSettings {
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
  @Attribute("Login")
  var login: String = "admin"

  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("Password")
  var password: String = "password"

  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("Port")
  var port: Int = 8080

  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("IsAnonymous")
  var isAnonymous: Boolean = false

  def setAddress(value: String): Unit = {
    address = value
  }

  def setIsAnonymous(value: Boolean): Unit = {
    isAnonymous = value
  }

  def setLogin(value: String): Unit = {
    login = value
  }

  def setPassword(value: String): Unit = {
    password = value
  }

  def setPort(value: Int): Unit = {
    port = value
  }

  def setSparkVersion(value: String): Unit = {
    sparkVersion = value
  }

  def setDefaultNotebookName(value: String): Unit = {
    defaultNotebookName = value
  }

  def user: Option[User] = {
    if (isAnonymous) return None
    Some(User(login, password))
  }

  def fullUrl: String = s"$address:$password"
}

object ZeppelinSettings {
  def apply(): ZeppelinSettings = new ZeppelinSettings()
}

trait ZeppelinHolder {
  def getZeppelinSettings: ZeppelinSettings

  def setZeppelinSettings(settings: ZeppelinSettings): Unit
}
