package jetbrains.zeppelin.settings

import com.intellij.util.xmlb.annotations.{Attribute, Property}

case class RemoteRunSettingsState() {
  @Property(surroundWithTag = false) var zeppelinSettings: ZeppelinSettings = ZeppelinSettings()
}

class ZeppelinSettings {
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
}

object ZeppelinSettings {
  def apply(): ZeppelinSettings = new ZeppelinSettings()
}

trait ZeppelinHolder {
  def getZeppelinSettings: ZeppelinSettings

  def setZeppelinSettings(settings: ZeppelinSettings): Unit
}
