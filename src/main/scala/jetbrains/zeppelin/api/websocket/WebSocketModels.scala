package jetbrains.zeppelin.api.websocket

import spray.json.{DefaultJsonProtocol, RootJsonFormat}


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


/**
  * The response web socket codes
  */
object ResponseCode extends Enumeration {
  type ResponseCode = Value
  val PARAGRAPH_UPDATE_OUTPUT, PARAGRAPH_APPEND_OUTPUT, PARAGRAPH, PARAGRAPH_ADDED, NOTE, PROGRESS = Value
}