package jetbrains.zeppelin.api

import spray.json.{DefaultJsonProtocol, RootJsonFormat}


case class Message(data: String, messageType: String)

case class Results(msg: List[Message] = List(), code: String = "")

case class Config(language: Option[String] = None, enabled: Option[Boolean] = None)

case class Notebook(id: String, name: String = "", paragraphs: List[Paragraph] = List())

case class Paragraph(id: String, jobName: String = "",
                     status: String = "", user: String = "", config: Config = Config(),
                     results: Option[Results] = None, title: Option[String] = None, text: Option[String] = None)


case class Credentials(principal: String, ticket: String, roles: String)

object CredentialsJsonProtocol extends DefaultJsonProtocol {

}

object ZeppelinAPIProtocol extends DefaultJsonProtocol {
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat2(Message)
  implicit val resultsFormat: RootJsonFormat[Results] = jsonFormat2(Results)
  implicit val configFormat: RootJsonFormat[Config] = jsonFormat2(Config)
  implicit val paragraphFormat: RootJsonFormat[Paragraph] = jsonFormat8(Paragraph)
  implicit val noteFormat: RootJsonFormat[Notebook] = jsonFormat3(Notebook)
  implicit val CredentialsFormat: RootJsonFormat[Credentials] = jsonFormat3(Credentials)
}

