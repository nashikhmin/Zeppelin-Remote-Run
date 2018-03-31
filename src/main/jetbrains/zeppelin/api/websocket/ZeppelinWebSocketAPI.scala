package jetbrains.zeppelin.api.websocket

import jetbrains.zeppelin.api._
import spray.json.{JsObject, _}

import scala.collection.mutable


private case class RunParagraphData(id: String,
                                    paragraph: Option[String],
                                    title: Option[String],
                                    settings: Map[String, String] = Map(),
                                    params: Map[String, String] = Map(),
                                    config: Map[String, String] = Map())

case class OutputResult(data: String, index: Int, noteId: String, paragraphId: String)


private object ZeppelinWebSocketProtocol extends DefaultJsonProtocol {
  implicit val RunParagraphFormat: RootJsonFormat[RunParagraphData] = jsonFormat6(RunParagraphData)
  implicit val OutputResultFormat: RootJsonFormat[OutputResult] = jsonFormat4(OutputResult)
}

trait OutputHandler {
  def handle(data: OutputResult, isAppend: Boolean)

  def onSuccess()

  def onError()
}


import jetbrains.zeppelin.api.ZeppelinAPIProtocol._

class ZeppelinWebSocketAPI(webSocketAPI: WebSocketAPI) {
  def this(address: String, port: Int) {
    this(new WebSocketAPI(s"ws://$address:$port/ws"))
    webSocketAPI.connect()
  }

  def getNote(noteId: String, credentials: Credentials): Notebook = {
    val data = Map("id" -> noteId)
    val opRequest = "GET_NOTE"
    val opResponse = "NOTE"
    val requestMessage = RequestMessage(opRequest, data.toJson, credentials)
    val response = webSocketAPI.doRequestSync(requestMessage, opResponse).fields.getOrElse("note", JsObject())
    response.convertTo[Notebook]
  }

  def runParagraph(paragraph: Paragraph, outputHandler: OutputHandler, credentials: Credentials) {
    import ZeppelinWebSocketProtocol._

    val data = RunParagraphData(paragraph.id, paragraph.text, paragraph.title)
    val handlers: mutable.Map[String, MessageHandler] = getRunParagraphHandlers(outputHandler: OutputHandler)

    val opRequest = "RUN_PARAGRAPH"

    val requestMessage = RequestMessage(opRequest, data.toJson, credentials)
    webSocketAPI.doRequestAsync(requestMessage, handlers)
  }

  private def getRunParagraphHandlers(outputHandler: OutputHandler) = {
    import ZeppelinWebSocketProtocol._
    val handlers: mutable.Map[String, MessageHandler] = mutable.Map()

    handlers.put("PARAGRAPH_UPDATE_OUTPUT", (response: ResponseMessage) => {
      val output = response.data.convertTo[OutputResult]
      outputHandler.handle(output, isAppend = false)
    })

    handlers.put("PARAGRAPH_APPEND_OUTPUT", (response: ResponseMessage) => {
      val output = response.data.convertTo[OutputResult]
      outputHandler.handle(output, isAppend = true)
    })

    handlers.put("PARAGRAPH", (result: ResponseMessage) => {
      val status = result.data.fields.getOrElse("paragraph", JsObject()).asJsObject().fields
        .getOrElse("status", JsString("")).convertTo[String]

      if (status == "FINISHED") {
        outputHandler.onSuccess()
      }
    })
    handlers
  }
}
