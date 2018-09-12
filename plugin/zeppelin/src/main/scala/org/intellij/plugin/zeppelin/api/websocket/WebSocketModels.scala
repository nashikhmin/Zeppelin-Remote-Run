package org.intellij.plugin.zeppelin.api.websocket

import org.intellij.plugin.zeppelin.models.Credentials
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}


case class RunParagraphData(id: String,
                            paragraph: Option[String],
                            title: Option[String],
                            settings: Map[String, String] = Map(),
                            params: Map[String, String] = Map(),
                            config: Map[String, String] = Map())

object ZeppelinWebSocketProtocol extends DefaultJsonProtocol {
  implicit val RunParagraphFormat: RootJsonFormat[RunParagraphData] = jsonFormat6(RunParagraphData)
}


/**
  * The response web socket codes
  */
object ResponseCode extends Enumeration {
  type ResponseCode = Value
  val PARAGRAPH_UPDATE_OUTPUT, PARAGRAPH_APPEND_OUTPUT, PARAGRAPH, PARAGRAPH_ADDED, NOTE, PROGRESS,
  SAVE_NOTE_FORMS, NOTES_INFO, INTERPRETER_BINDINGS, PARAS_INFO = Value
}

case class SessionIsClosedException() extends Exception()

case class RequestMessage(op: String, data: JsValue, credentials: Credentials)

case class ResponseMessage(op: String, data: JsObject)

trait MessageHandler {
  def handle(result: ResponseMessage)
}

object WebSocketApiProtocol extends DefaultJsonProtocol {

  implicit object RequestMessageFormat extends RootJsonFormat[RequestMessage] {
    def read(value: JsValue): RequestMessage = {
      throw DeserializationException("Non implemented")
    }

    def write(r: RequestMessage): JsObject = {
      JsObject(
        "op" -> JsString(r.op),
        "data" -> r.data,
        "ticket" -> JsString(r.credentials.ticket),
        "principal" -> JsString(r.credentials.principal),
        "roles" -> JsString(r.credentials.roles)
      )
    }
  }


  implicit object ResponseMessageFormat extends RootJsonFormat[ResponseMessage] {
    def read(value: JsValue): ResponseMessage = {
      value.asJsObject.getFields("op", "data") match {
        case Seq(JsString(op), data) => ResponseMessage(op, data.asJsObject())
        case _ => throw DeserializationException("Response message expected")
      }
    }

    def write(r: ResponseMessage): JsValue = {
      throw throw DeserializationException("Non implemented")
    }
  }

}
