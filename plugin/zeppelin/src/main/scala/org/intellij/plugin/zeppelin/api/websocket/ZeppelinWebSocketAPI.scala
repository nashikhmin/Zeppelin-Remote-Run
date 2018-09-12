package org.intellij.plugin.zeppelin.api.websocket

import com.intellij.openapi.diagnostic.Logger
import org.intellij.plugin.zeppelin.models
import org.intellij.plugin.zeppelin.models.ZeppelinAPIProtocol._
import org.intellij.plugin.zeppelin.models._
import spray.json.{JsObject, _}

/**
  * The service for communication with the Zeppelin application by WebSockets
  *
  * @param webSocketAPI - web socket client
  */
class ZeppelinWebSocketAPI private(webSocketAPI: WebSocketAPI) {
  private val LOG = Logger.getInstance(getClass)

  {
    List(ResponseCode.PARAGRAPH_ADDED, ResponseCode.PARAS_INFO, ResponseCode.SAVE_NOTE_FORMS, ResponseCode.NOTES_INFO)
      .foreach(code => webSocketAPI.registerHandler(code.toString, (_: ResponseMessage) => Unit))
  }

  /**
    * The response web socket codes
    */
  object RequestOperations extends Enumeration {
    type RequestOperations = Value
    val GET_NOTE, RUN_PARAGRAPH, GET_INTERPRETER_BINDINGS, SAVE_INTERPRETER_BINDINGS = Value
  }

  /**
    * Close the connection
    */
  def close(): Unit = {
    webSocketAPI.close()
    if (LOG.isTraceEnabled) LOG.trace("Connection is closed")
  }

  /**
    * Connect to the application
    */
  def connect(): Unit = {
    webSocketAPI.connect()
    if (LOG.isTraceEnabled) LOG.trace("Connection is opened")
  }

  def connectionStatus: models.ConnectionStatus.Value = webSocketAPI.status

  /**
    * Get list of interpreters that are available for the notebooks
    *
    * @param noteId      - id of the notebook
    * @param credentials - an user credentials
    * @return
    */
  def getBindingInterpreters(noteId: String, credentials: Credentials): List[InterpreterBindings] = {
    val data = Map("noteId" -> noteId)
    if (LOG.isTraceEnabled) {
      LOG
        .trace(s"Start request 'Get bindings interpreters'. Data : $data, crediantials: $credentials.")
    }
    val opRequest = RequestOperations.GET_INTERPRETER_BINDINGS.toString
    val opResponse = ResponseCode.INTERPRETER_BINDINGS.toString
    val requestMessage = RequestMessage(opRequest, data.toJson, credentials)
    val response = webSocketAPI.doRequestSync(requestMessage, opResponse).fields
      .getOrElse("interpreterBindings", JsArray())
    if (LOG.isTraceEnabled) LOG.trace(s"End of request 'Get bindings interpreters'. Response: $response.")
    response.convertTo[List[InterpreterBindings]]
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
    if (LOG.isTraceEnabled) LOG.trace(s"Start request 'Get note'. Data : $data, crediantials: $credentials.")
    val opRequest = RequestOperations.GET_NOTE.toString
    val opResponse = ResponseCode.NOTE.toString
    val requestMessage = RequestMessage(opRequest, data.toJson, credentials)
    val response = webSocketAPI.doRequestSync(requestMessage, opResponse).fields.getOrElse("note", JsObject())
    if (LOG.isTraceEnabled) LOG.trace(s"End of request 'Get note'. Response: $response.")
    response.convertTo[Notebook]
  }

  def registerHandler(op: String, handler: MessageHandler) {
    webSocketAPI.registerHandler(op, handler)
  }

  /**
    * Run the paragraph in the Zeppelin application
    *
    * @param paragraph   - the paragraph, which must be run
    * @param credentials - the credentials of the user
    */
  def runParagraph(paragraph: Paragraph, credentials: Credentials): Unit = {
    import ZeppelinWebSocketProtocol._

    val data = RunParagraphData(paragraph.id, paragraph.text, paragraph.title)
    if (LOG.isTraceEnabled) LOG.trace(s"Start request 'Run paragraph'. Data : $data, crediantials: $credentials.")

    val opRequest = RequestOperations.RUN_PARAGRAPH.toString
    val requestMessage = RequestMessage(opRequest, data.toJson, credentials)
    webSocketAPI.doRequestAsync(requestMessage)
  }

  /**
    * Save new interpreters bindings settings for the notebook
    *
    * @param noteId                  - id of the notebook
    * @param newInterpretersBindings - new interpreters bindings
    * @param credentials             - an user credentials
    */
  def saveListOfBindingInterpreters(noteId: String,
                                    newInterpretersBindings: List[String],
                                    credentials: Credentials): Unit = {
    val data: Map[String, JsValue] = Map("noteId" -> noteId.toJson,
      "selectedSettingIds" -> newInterpretersBindings.toJson)
    if (LOG.isTraceEnabled) {
      LOG
        .trace(s"Start request 'Save list of binding interpreters'. Data : $data, crediantials: $credentials.")
    }

    val opRequest = RequestOperations.SAVE_INTERPRETER_BINDINGS.toString
    val requestMessage = RequestMessage(opRequest, data.toJson, credentials)
    webSocketAPI.doRequestWithoutWaitingResult(requestMessage)
  }



}

object ZeppelinWebSocketAPI {
  def apply(address: String, port: Int): ZeppelinWebSocketAPI = {
    new ZeppelinWebSocketAPI(new WebSocketAPI(s"ws://$address:$port/ws"))
  }
}