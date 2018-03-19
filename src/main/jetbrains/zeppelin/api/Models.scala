package jetbrains.zeppelin.api

import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat, pimpAny}


case class Message(data: String, messageType: String)

case class Results(msg: List[Message] = List(), code: String = "")

case class Config(language: Option[String] = Option("scala"),
                  enabled: Option[Boolean] = Option(true))

case class Notebook(id: String, name: String = "", paragraphs: List[Paragraph] = List())

case class Paragraph(id: String, jobName: String = "",
                     status: String = "", user: String = "", config: Map[String, String] = Map(),
                     results: Option[Results] = None, title: Option[String] = None, text: Option[String] = None)


case class Credentials(principal: String, ticket: String, roles: String)

/**
  * Model for new paragraph request by REST API
  */
case class NewParagraph(title: String, text: String, config: Config = Config())

/**
  * Model for new notebook request by REST API
  */
case class NewNotebook(name: String, paragraphs: List[NewParagraph] = List())


object ZeppelinAPIProtocol extends DefaultJsonProtocol {
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat2(Message)
  implicit val resultsFormat: RootJsonFormat[Results] = jsonFormat2(Results)
  implicit val configFormat: RootJsonFormat[Config] = jsonFormat2(Config)
  implicit val paragraphFormat: RootJsonFormat[Paragraph] = jsonFormat8(Paragraph)
  implicit val noteFormat: RootJsonFormat[Notebook] = jsonFormat3(Notebook)
  implicit val CredentialsFormat: RootJsonFormat[Credentials] = jsonFormat3(Credentials)

  implicit object NewNotebookFormat extends RootJsonFormat[NewNotebook] {
    def write(note: NewNotebook): JsObject = {
      JsObject(
        "name" -> JsString(note.name),
        "paragraphs" -> note.paragraphs.toJson
      )
    }

    def read(value: JsValue): NewNotebook = {
      throw DeserializationException("Non implemented")
    }
  }

  implicit val NewParagraphFormat: RootJsonFormat[NewParagraph] = jsonFormat3(NewParagraph)
}

