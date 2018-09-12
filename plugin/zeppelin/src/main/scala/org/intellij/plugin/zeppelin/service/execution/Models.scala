package org.intellij.plugin.zeppelin.service.execution

import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

case class ProgressResponse(id: String, progress: Int)

case class ParagraphResponse(id: String, status: String, results: Option[ExecutionResults])

case class ParagraphAppendResponse(id: String)

case class ParagraphUpdateOutputResponse(data: String, noteId: String, paragraphId: String, index: Int)

case class ExecutionResults(code: String = "", msg: List[ExecutionResultsMsg] = List())

case class ExecutionResultsMsg(resultType: String, data: String)

case class OutputResponse(data: String, index: Int, noteId: String, paragraphId: String)


object ExecutionModelsFormatProtocol extends DefaultJsonProtocol {

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

  implicit val ExecutionResultsFormat: RootJsonFormat[ExecutionResults] = jsonFormat2(ExecutionResults)

  implicit val ParagraphAppendResponseFormat: RootJsonFormat[ParagraphAppendResponse] =
    jsonFormat1(ParagraphAppendResponse)


  implicit val ParagraphResponseFormat: RootJsonFormat[ParagraphResponse] = jsonFormat3(ParagraphResponse)

  implicit val ProgressResponseFormat: RootJsonFormat[ProgressResponse] = jsonFormat2(ProgressResponse)
  implicit val OutputResultFormat: RootJsonFormat[OutputResponse] = jsonFormat4(OutputResponse)
}

object ExecutionModelConverter {

  import ExecutionModelsFormatProtocol._

  def getOutputResult(jsObject: JsObject): OutputResponse = {
    jsObject.convertTo[OutputResponse]
  }

  def getParagraphResponse(jsObject: JsObject): ParagraphResponse = {
    jsObject.fields("paragraph").convertTo[ParagraphResponse]
  }

  def getProgressResponse(jsObject: JsObject): ProgressResponse = {
    jsObject.convertTo[ProgressResponse]
  }
}