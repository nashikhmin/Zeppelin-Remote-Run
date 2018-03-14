package jetbrains.zeppelin.api


case class Message(op: String, data: Map[String, String] = Map(), ticket: String = "anonymous", principal: String = "anonymous", roles: String = "") {
  private val a: String = ""
}

