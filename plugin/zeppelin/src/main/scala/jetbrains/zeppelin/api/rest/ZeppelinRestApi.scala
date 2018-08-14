package jetbrains.zeppelin.api.rest

import java.net.HttpCookie

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
  var sessionToken: Option[HttpCookie] = None
  var loginStatus: LoginStatus.LoginStatus = LoginStatus.NOT_LOGGED

  /**
    * Create a new notebook in Zeppelin
    *
    * @param newNotebook - a model of new notebook
    * @return the model of the created notebook
    */
  def createNotebook(newNotebook: NewNotebook): Notebook = {
    val response: HttpResponse[String] = restApi.performPostData("/notebook", newNotebook.toJson, sessionToken)
    if (response.code != 200) {
      throw RestApiException(s"Cannot create a notebook.", response.code)
    }

    val id = response.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Notebook(id)
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
    val response: HttpResponse[String] = restApi.performPostData(s"/notebook/$noteId/paragraph", data, sessionToken)

    if (response.code != 200) {
      throw RestApiException(s"Cannot create a paragraph", response.code)
    }

    val paragraphId = response.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Paragraph(paragraphId)
  }

  /**
    * Delete a notebook in Zeppelin
    *
    * @param noteId - an id of a notebook
    */
  def deleteNotebook(noteId: String): Unit = {
    val response: HttpResponse[String] = restApi.performDeleteData(s"/notebook/$noteId", sessionToken)
    if (response.code != 200) {
      throw RestApiException(s"Cannot delete a paragraph", response.code)
    }
  }

  /**
    * Delete a paragraph in Zeppelin
    *
    * @param noteId      - an id of a notebook
    * @param paragraphId - an id of a paragraph
    */
  def deleteParagraph(noteId: String, paragraphId: String): Unit = {
    val response: HttpResponse[String] = restApi
      .performDeleteData(s"/notebook/$noteId/paragraph/$paragraphId", sessionToken)
    if (response.code != 200) {
      throw RestApiException(s"Cannot delete a paragraph", response.code)
    }
  }

  /**
    * Get all available interpreters in Zeppelin
    *
    * @return a interpreters list
    */
  def getInterpreters: List[Interpreter] = {
    val response = restApi.performGetRequest("/interpreter/setting", sessionToken)
    if (response.code != 200) {
      throw RestApiException("Cannot get list of interpreters.", response.code)
    }

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
    val response = restApi.performGetRequest("/notebook", sessionToken)
    if (response.code != 200) {
      throw RestApiException("Cannot get list of notebooks", response.code)
    }

    val arrayList = response.body.parseJson.asJsObject.fields.getOrElse("body", JsArray())
    arrayList.convertTo[List[Map[String, String]]]
      .map(it => Notebook(it.getOrElse("id", ""), it.getOrElse("name", "")))
      .filter(it => !it.name.startsWith("~Trash") && it.name.startsWith(prefix))
  }

  def login(userName: String, password: String): Credentials = {
    val response: HttpResponse[String] = restApi
      .performPostForm("/login", Map("userName" -> userName, "password" -> password))
    if (response.code != 200) {
      throw RestApiException("Cannot login.", response.code)
    }
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
    val data: Map[String, String] = if (noteId.nonEmpty) Map("noteId" -> noteId) else Map()
    val response: HttpResponse[String] = restApi
      .performPutData(s"/interpreter/setting/restart/${interpreter.id} ", data.toJson, sessionToken)

    if (response.code != 200) {
      throw RestApiException("Cannot interpreter settings.", response.code)
    }
  }

  def updateInterpreterSettings(interpreter: Interpreter): Unit = {
    val response: HttpResponse[String] = restApi
      .performPutData(s"/interpreter/setting/${interpreter.id} ", interpreter.toJson, sessionToken)

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