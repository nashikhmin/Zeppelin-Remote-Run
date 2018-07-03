package jetbrains.zeppelin.api.websocket

import jetbrains.zeppelin.api
import jetbrains.zeppelin.api.ZeppelinAPIProtocol._
import jetbrains.zeppelin.api._
import spray.json.{JsObject, _}

/**
  * The service for communication with the Zeppelin application by WebSockets
  *
  * @param webSocketAPI - web socket client
  */
class ZeppelinWebSocketAPI private(webSocketAPI: WebSocketAPI) {
  {
    List(ResponseCode.PARAGRAPH_ADDED, ResponseCode.PROGRESS, ResponseCode.SAVE_NOTE_FORMS, ResponseCode.NOTES_INFO)
      .foreach(code => webSocketAPI.registerHandler(code.toString, (_: ResponseMessage) => Unit))
  }

  def connectionStatus: api.ConnectionStatus.Value = webSocketAPI.status

  /**
    * Close the connection
    */
  def close(): Unit = {
    webSocketAPI.close()
  }

  /**
    * Connect to the application
    */
  def connect(): Unit = {
    webSocketAPI.connect()
  }

  /**
    * Get model of the notebook
    *
    * @param noteId      - the id of the notebook
    * @param credentials - the credentials of the user
    * @return the model of the notebook
    */
  def getNote(noteId: String, credentials: Credentials): Notebook = {
    val data = Map("id" -> noteId)
    val opRequest = RequestOperations.GET_NOTE.toString
    val opResponse = ResponseCode.NOTE.toString
    val requestMessage = RequestMessage(opRequest, data.toJson, credentials)
    val response = webSocketAPI.doRequestSync(requestMessage, opResponse).fields.getOrElse("note", JsObject())
    response.convertTo[Notebook]
  }

  /**
    * Run the paragraph in the Zeppelin application
    *
    * @param paragraph     - the paragraph, which must be run
    * @param outputHandler - the handle to get the result of the run
    * @param credentials   - the credentials of the user
    */
  def runParagraph(paragraph: Paragraph, outputHandler: OutputHandler, credentials: Credentials) {
    import ZeppelinWebSocketProtocol._

    val data = RunParagraphData(paragraph.id, paragraph.text, paragraph.title)
    val handlers = runParagraphHandlers(outputHandler: OutputHandler)

    val opRequest = RequestOperations.RUN_PARAGRAPH.toString

    val requestMessage = RequestMessage(opRequest, data.toJson, credentials)
    webSocketAPI.doRequestAsync(requestMessage, handlers.map { case (key, value) => (key.toString, value) })
  }

  /**
    * Get the run paragraph handlers
    *
    * @param outputHandler - handler that got the output of the process
    * @return the handler which handle the web socket requests
    */
  private def runParagraphHandlers(outputHandler: OutputHandler) = {
    import ZeppelinWebSocketProtocol._


    def handleUpdateOutput: MessageHandler = {
      response: ResponseMessage => {
        val output = response.data.convertTo[OutputResult]
        outputHandler.handle(output, isAppend = false)
      }
    }

    def handleAppendOutput: MessageHandler = {
      response: ResponseMessage => {
        val output = response.data.convertTo[OutputResult]
        outputHandler.handle(output, isAppend = true)
      }
    }

    def handleParagraph: MessageHandler = {
      result: ResponseMessage => {
        val status = result.data.fields.getOrElse("paragraph", JsObject()).asJsObject().fields
          .getOrElse("status", JsString("")).convertTo[String]

        status match {
          case "FINISHED" => outputHandler.onSuccess()
          case "ERROR" => outputHandler.onError()
          case _ => Unit
        }
      }
    }

    Map[ResponseCode.Value, MessageHandler](
      ResponseCode.PARAGRAPH_UPDATE_OUTPUT -> handleUpdateOutput,
      ResponseCode.PARAGRAPH_APPEND_OUTPUT -> handleAppendOutput,
      ResponseCode.PARAGRAPH -> handleParagraph
    )
  }

  /**
    * The response web socket codes
    */
  object RequestOperations extends Enumeration {
    type RequestOperations = Value
    val GET_NOTE, RUN_PARAGRAPH = Value
  }

}

object ZeppelinWebSocketAPI {
  def apply(address: String, port: Int): ZeppelinWebSocketAPI = {
    new ZeppelinWebSocketAPI(new WebSocketAPI(s"ws://$address:$port/ws"))
  }
}
