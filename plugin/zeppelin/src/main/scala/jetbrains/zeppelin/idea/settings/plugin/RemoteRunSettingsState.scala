package jetbrains.zeppelin.idea.settings.plugin

import com.intellij.util.xmlb.annotations.{Attribute, Property}
import jetbrains.zeppelin.models.SparkVersion

case class RemoteRunSettingsState() {
  @Property(surroundWithTag = false) var zeppelinSettings: ZeppelinSettings = ZeppelinSettings()
}

class ZeppelinSettings {
  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("sparkVersion")
  var sparkVersion: String = SparkVersion.ZEPPELIN_DEFAULT_VERSION.toString

  @SuppressWarnings(Array("Address"))
  @Attribute("UriEnabled")
  var address: String = "localhost"

  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("Login")
  var login: String = ""

  @SuppressWarnings(Array("FieldMayBeFinal"))
  @Attribute("Password")
  var password: String = ""

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
}

object ZeppelinSettings {
  def apply(): ZeppelinSettings = new ZeppelinSettings()
}

trait ZeppelinHolder {
  def getZeppelinSettings: ZeppelinSettings

  def setZeppelinSettings(settings: ZeppelinSettings): Unit
}
