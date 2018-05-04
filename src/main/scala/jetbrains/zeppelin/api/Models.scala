package jetbrains.zeppelin.api

import jetbrains.zeppelin.api
import jetbrains.zeppelin.api.InterpreterStatus.InterpreterStatus
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

case class Config(enabled: Option[Boolean] = Some(true))


// Define a new enumeration with a type alias and work with the full set of enumerated values
object InterpreterStatus extends Enumeration {
  type InterpreterStatus = Value
  val READY, DOWNLOADING_DEPENDENCIES, ERROR = Value
}

case class Interpreter(id: String,
                       name: String,
                       group: String,
                       dependencies: List[Dependency],
                       status: InterpreterStatus,
                       properties: JsObject,
                       option: JsObject,
                       interpreterGroup: List[JsObject],
                       errorReason: Option[String])

case class Dependency(var groupArtifactVersion: String,
                      exclusions: Option[List[String]] = Some(List.empty),
                      local: Option[Boolean] = Some(true))

case class Notebook(id: String, name: String = "", paragraphs: List[Paragraph] = List())

case class Paragraph(id: String, jobName: String = "",
                     status: String = "",
                     user: String = "",
                     config: Config = Config(),
                     title: Option[String] = None,
                     text: Option[String] = None)


case class Credentials(principal: String, ticket: String, roles: String)


/**
  * Model for new notebook request by REST API
  */
case class NewNotebook(name: String)


object ZeppelinAPIProtocol extends DefaultJsonProtocol {
  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] = {
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)

      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }
  }

  implicit val configFormat: RootJsonFormat[Config] = jsonFormat(Config, "enabled")
  implicit val paragraphFormat: RootJsonFormat[Paragraph] = jsonFormat7(Paragraph)
  implicit val noteFormat: RootJsonFormat[Notebook] = jsonFormat3(Notebook)
  implicit val CredentialsFormat: RootJsonFormat[Credentials] = jsonFormat3(Credentials)
  implicit val NewNotebookFormat: RootJsonFormat[NewNotebook] = jsonFormat1(NewNotebook)
  implicit val DependencyFormat: RootJsonFormat[Dependency] = jsonFormat3(Dependency)
  implicit val InterpreterStatusFormat: RootJsonFormat[api.InterpreterStatus.Value] = enumFormat(InterpreterStatus)
  implicit val InterpreterFormat: RootJsonFormat[Interpreter] = jsonFormat9(Interpreter)
}

