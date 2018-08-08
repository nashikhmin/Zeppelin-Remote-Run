package jetbrains.zeppelin.api

import jetbrains.zeppelin.api
import jetbrains.zeppelin.api.InterpreterStatus.InterpreterStatus
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

import scala.util.Try

case class Config(enabled: Option[Boolean] = Some(true))


object InterpreterStatus extends Enumeration {
  type InterpreterStatus = Value
  val READY, DOWNLOADING_DEPENDENCIES, ERROR = Value
}


case class InterpreterBindings(
                                id: String,
                                name: String,
                                interpreters: List[InterpreterBinding]
                              )

case class InterpreterBinding(
                               defaultInterpreter: Boolean,
                               name: String
                             )


object InstantiationType extends Enumeration {
  type InstantiationType = Value
  val SHARED: api.InstantiationType.Value = Value("shared")
  val SCOPED: api.InstantiationType.Value = Value("scoped")
  val ISOLATED: api.InstantiationType.Value = Value("isolated")
}

case class InterpreterOption(perNote: Option[String] = Some(InstantiationType.SHARED.toString),
                             perUser: Option[String] = Some(InstantiationType.SHARED.toString)) {
  def isGlobally: Boolean = {
    perNoteAsEnum == InstantiationType.SHARED && perUserAsEnum == InstantiationType.SHARED
  }

  def perNoteAsEnum: InstantiationType.Value = {
    getValue(perNote)
  }

  def perNoteAsString: String = {
    perNoteAsEnum.toString
  }

  def perUserAsEnum: InstantiationType.Value = {
    getValue(perUser)
  }

  def perUserAsString: String = {
    perUserAsEnum.toString
  }

  private def getValue(value: Option[String]) = {
    val raw = value.getOrElse("")
    val enumValue = Try {
      InstantiationType.withName(raw)
    }.getOrElse(InstantiationType.SHARED)
    enumValue
  }
}

case class Interpreter(id: String,
                       name: String,
                       group: String,
                       dependencies: List[Dependency],
                       status: InterpreterStatus,
                       properties: JsObject,
                       option: InterpreterOption,
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

case class ExecutionResults(code: String = "", msg: List[ExecutionResultsMsg] = List())

case class ExecutionResultsMsg(resultType: String, data: String)

/**
  * Model for new notebook request by REST API
  */
case class NewNotebook(name: String)


object ZeppelinAPIProtocol extends DefaultJsonProtocol {

  implicit object ExecutionResultsMsgFormat extends RootJsonFormat[ExecutionResultsMsg] {
    def read(value: JsValue): ExecutionResultsMsg = {
      value.asJsObject.getFields("type", "data") match {
        case Seq(JsString(resultType), JsString(data)) => ExecutionResultsMsg(resultType, data)
        case _ => throw DeserializationException("Response message expected")
      }
    }

    def write(r: ExecutionResultsMsg): JsValue = {
      throw throw DeserializationException("Non implemented")
    }
  }

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
  implicit val InterpreterOptionFormat: RootJsonFormat[InterpreterOption] = jsonFormat2(InterpreterOption)
  implicit val InterpreterFormat: RootJsonFormat[Interpreter] = jsonFormat9(Interpreter)
  implicit val InterpreterBindingFormat: RootJsonFormat[InterpreterBinding] = jsonFormat2(InterpreterBinding)
  implicit val InterpreterBindingsFormat: RootJsonFormat[InterpreterBindings] = jsonFormat3(InterpreterBindings)
  implicit val ExecutionResultsFormat: RootJsonFormat[ExecutionResults] = jsonFormat2(ExecutionResults)
}


/**
  * The connection status to the server
  */
object ConnectionStatus extends Enumeration {
  type ConnectionStatus = Value
  val CONNECTED, FAILED, DISCONNECTED = Value
}

/**
  * Login status
  */
object LoginStatus extends Enumeration {
  type LoginStatus = Value
  val LOGGED, NOT_LOGGED = Value
}

class ZeppelinException() extends Exception {
  override def getMessage: String = {
    s"Error during Zeppelin Exception."
  }
}

case class ZeppelinConnectionException(uri: String) extends ZeppelinException {
  override def getMessage: String = {
    s"Cannot connect to the Zeppelin app. " +
      s"Check the availability of web socket connection to the service $uri"
  }
}

case class ZeppelinLoginException() extends ZeppelinException {
  override def getMessage: String = {
    s"Cannot login to the Zeppelin app. " +
      s"The login or the password is wrong."
  }
}


case class User(login: String, password: String)