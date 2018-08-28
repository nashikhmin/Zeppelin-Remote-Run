package jetbrains.zeppelin.api.rest

import java.net.HttpCookie

import com.intellij.openapi.diagnostic.Logger
import jetbrains.zeppelin.models.ZeppelinAPIProtocol._
import jetbrains.zeppelin.models._
import scalaj.http.HttpResponse
import spray.json._

/**
  * The service to work with Zeppelin by the RESt API
  *
  * @param restApi - REST service
  */
class ZeppelinRestApi private(val restApi: RestAPI) {
  private val LOG = Logger.getInstance(getClass)
  var sessionToken: Option[HttpCookie] = None
  var loginStatus: LoginStatus.LoginStatus = LoginStatus.NOT_LOGGED

  /**
    * Create a new notebook in Zeppelin
    *
    * @param newNotebook - a model of new notebook
    * @return the model of the created notebook
    */
  def createNotebook(newNotebook: NewNotebook): Notebook = {
    if (LOG.isTraceEnabled) LOG.trace(s"Start perform request 'Create notebook $newNotebook'")
    val response: HttpResponse[String] = restApi.performPostData("/notebook", newNotebook.toJson, sessionToken)

    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Create notebook' $response")
    if (response.code != 200) {
      throw RestApiException(s"Cannot create notebook.", response.code)
    }

    val id = response.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Notebook(id, newNotebook.name)
  }

  /**
    * Create a paragraph in Zeppelin
    *
    * @param noteId        - id of a notebook
    * @param paragraphText - a text, which be put in the paragraph
    * @return a model of paragraph
    */
  def createParagraph(noteId: String, paragraphText: String): Paragraph = {
    val data = Map("title" -> "", "text" -> paragraphText).toJson
    if (LOG.isTraceEnabled) LOG.trace(s"Start perform request 'Create paragraph'. Data: $data")
    val response: HttpResponse[String] = restApi.performPostData(s"/notebook/$noteId/paragraph", data, sessionToken)

    if (response.code != 200) {
      throw RestApiException(s"Cannot create a paragraph", response.code)
    }

    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Create paragraph' $response")
    val paragraphId = response.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Paragraph(paragraphId)
  }

  /**
    * Delete a notebook in Zeppelin
    *
    * @param noteId - an id of a notebook
    */
  def deleteNotebook(noteId: String): Unit = {
    if (LOG.isTraceEnabled) LOG.trace(s"Start perform request 'Delete notebook'. NoteId: $noteId")
    val response: HttpResponse[String] = restApi.performDeleteData(s"/notebook/$noteId", sessionToken)
    if (response.code != 200) {
      throw RestApiException(s"Cannot delete a paragraph", response.code)
    }
    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Delete notebook' $response")
  }

  /**
    * Delete a paragraph in Zeppelin
    *
    * @param noteId      - an id of a notebook
    * @param paragraphId - an id of a paragraph
    */
  def deleteParagraph(noteId: String, paragraphId: String): Unit = {
    if (LOG.isTraceEnabled) LOG.trace(s"Start perform request 'Delete paragraph'. NoteId: $noteId")
    val response: HttpResponse[String] = restApi
      .performDeleteData(s"/notebook/$noteId/paragraph/$paragraphId", sessionToken)
    if (response.code != 200) {
      throw RestApiException(s"Cannot delete a paragraph", response.code)
    }
    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Delete paragraph' $response")
  }

  /**
    * Get all available interpreters in Zeppelin
    *
    * @return a interpreters list
    */
  def getInterpreters: List[Interpreter] = {
    if (LOG.isTraceEnabled) LOG.trace(s"Start perform request 'Get interpreters'")
    val response = restApi.performGetRequest("/interpreter/setting", sessionToken)
    if (response.code != 200) {
      throw RestApiException("Cannot get list of interpreters.", response.code)
    }
    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Get interpreters' $response")
    val arrayList = response.body.parseJson.asJsObject.fields.getOrElse("body", JsArray())
    arrayList.convertTo[List[Interpreter]]
  }

  /**
    * Get Notebooks by prefix
    *
    * @param prefix - prefix of a notebook name
    * @return the notebooks with the names which start from the prefix
    */
  def getNotebooks(prefix: String = ""): List[Notebook] = {
    if (LOG.isTraceEnabled) LOG.trace(s"Start perform request 'Get notebooks'")
    val response = restApi.performGetRequest("/notebook", sessionToken)
    if (response.code != 200) {
      throw RestApiException("Cannot get list of notebooks", response.code)
    }
    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Get notebooks' $response")
    val arrayList = response.body.parseJson.asJsObject.fields.getOrElse("body", JsArray())
    arrayList.convertTo[List[Map[String, String]]]
      .map(it => Notebook(it.getOrElse("id", ""), it.getOrElse("name", "")))
      .filter(it => !it.name.startsWith("~Trash") && it.name.startsWith(prefix))
  }

  /**
    * Login to Zeppelin
    *
    * @param userName - a user name
    * @param password - a user password
    * @return - credentials to perform REST operations
    */
  def login(userName: String, password: String): Credentials = {
    if (LOG.isTraceEnabled) LOG.trace(s"Start perform request 'Login' User:$userName")
    val response: HttpResponse[String] = restApi
      .performPostForm("/login", Map("userName" -> userName, "password" -> password))
    if (response.code != 200) {
      throw RestApiException("Cannot login.", response.code)
    }
    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Login' Response:$response")
    val json = response.body.parseJson.asJsObject.fields.getOrElse("body", JsObject())
    sessionToken = response.cookies.reverseIterator.find(_.getName == "JSESSIONID")
    loginStatus = LoginStatus.LOGGED
    json.convertTo[Credentials]
  }

  /**
    * Restart the selected interpreter
    *
    * @param interpreter - an interpreter which must be restarted
    * @param noteId      - an id of the selected notebook
    */
  def restartInterpreter(interpreter: Interpreter, noteId: String = ""): Unit = {
    if (LOG.isTraceEnabled) {
      LOG
        .trace(s"Start perform request 'Restart interpreter' Interpreter:$interpreter, note id: $noteId")
    }
    val data: Map[String, String] = if (noteId.nonEmpty) Map("noteId" -> noteId) else Map()
    val response: HttpResponse[String] = restApi
      .performPutData(s"/interpreter/setting/restart/${interpreter.id} ", data.toJson, sessionToken)
    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Restart interpreter' Response:$response")
    if (response.code != 200) {
      throw RestApiException("Cannot interpreter settings.", response.code)
    }
  }

  /**
    * Update the settings of an interpreter
    *
    * @param interpreter - a model of interpreter with new settings
    */
  def updateInterpreterSettings(interpreter: Interpreter): Unit = {
    if (LOG.isTraceEnabled) LOG.trace(s"Start perform request 'Update interpreter settings' Interpreter:$interpreter")
    val response: HttpResponse[String] = restApi
      .performPutData(s"/interpreter/setting/${interpreter.id} ", interpreter.toJson, sessionToken)
    if (LOG.isTraceEnabled) LOG.trace(s"Performed request 'Update interpreter settings' Response:$response")
    if (response.code != 200) {
      throw RestApiException("Cannot interpreter settings.", response.code)
    }
  }
}

object ZeppelinRestApi {
  def apply(host: String, port: Int): ZeppelinRestApi = {
    new ZeppelinRestApi(new RestAPI(host, port))
  }
}