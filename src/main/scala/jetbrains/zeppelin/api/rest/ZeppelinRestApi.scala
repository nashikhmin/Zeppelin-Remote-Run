package jetbrains.zeppelin.api.rest

import java.net.HttpCookie

import jetbrains.zeppelin.api.ZeppelinAPIProtocol._
import jetbrains.zeppelin.api._
import scalaj.http.HttpResponse
import spray.json._

/**
  * The service to work with Zeppelin by the RESt API
  *
  * @param restApi - REST service
  */
class ZeppelinRestApi private(val restApi: RestAPI) {
  var sessionToken: Option[HttpCookie] = None

  /**
    * Create a new notebook in Zeppelin
    *
    * @param newNotebook - a model of new notebook
    * @return the model of the created notebook
    */
  def createNotebook(newNotebook: NewNotebook): Notebook = {
    val result: HttpResponse[String] = restApi.performPostData("/notebook", newNotebook.toJson, sessionToken)
    if (result.code != 201)
      throw RestApiException(s"Cannot create a notebook.\n Error code: ${result.code}.\nBody:${result.body}")

    val id = result.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Notebook(id)
  }

  /**
    * Get Notebooks by prefix
    *
    * @param prefix - prefix of a notebook name
    * @return the notebooks with the names which start from the prefix
    */
  def getNotebooks(prefix: String = ""): List[Notebook] = {
    val result = restApi.performGetRequest("/notebook", sessionToken)
    if (result.code != 200)
      throw RestApiException(s"Cannot get list of notebooks.\n Error code: ${result.code}.\nBody:${result.body}")

    val arrayList = result.body.parseJson.asJsObject.fields.getOrElse("body", JsArray())
    val list = arrayList.convertTo[List[Map[String, String]]]
      .map(it => Notebook(it.getOrElse("id", ""), it.getOrElse("name", "")))
      .filter(it => !it.name.startsWith("~Trash") && it.name.startsWith(prefix))
    list
  }

  /**
    * Create a pragraph in Zeppelin
    *
    * @param noteId        - id of a notebook
    * @param paragraphText - a text, which be put in the paragraph
    * @return a model of paragraph
    */
  def createParagraph(noteId: String, paragraphText: String): Paragraph = {
    val data = Map("title" -> "", "text" -> paragraphText).toJson
    val response: HttpResponse[String] = restApi.performPostData(s"/notebook/$noteId/paragraph", data, sessionToken)

    if (response.code != 201)
      throw RestApiException(s"Cannot create a paragraph.\n Error code: ${response.code}.\nBody:${response.body}")

    val paragraphId = response.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Paragraph(paragraphId)
  }

  def updateInterpreterSettings(interpreter: Interpreter): Unit = {
    val response: HttpResponse[String] = restApi
      .performPutData(s"/interpreter/setting/${interpreter.id} ", interpreter.toJson, sessionToken)

    if (response.code != 200)
      throw RestApiException(s"Cannot interpreter settings.\n Error code: ${response.code}.\nBody:${response.body}")
  }

  def login(userName: String, password: String): Credentials = {
    val result: HttpResponse[String] = restApi
      .performPostForm("/login", Map("userName" -> userName, "password" -> password))
    if (result.code != 200)
      throw RestApiException(s"Cannot login.\n Error code: ${result.code}.\nBody:${result.body}")
    val json = result.body.parseJson.asJsObject.fields.getOrElse("body", JsObject())
    sessionToken = result.cookies.reverseIterator.find(_.getName == "JSESSIONID")
    json.convertTo[Credentials]
  }

  def getInterpreters: List[Interpreter] = {
    val result = restApi.performGetRequest("/interpreter/setting", sessionToken)
    if (result.code != 200)
      throw RestApiException(s"Cannot get list of interpreters.\n Error code: ${result.code}.\nBody:${result.body}")

    val arrayList = result.body.parseJson.asJsObject.fields.getOrElse("body", JsArray())
    arrayList.convertTo[List[Interpreter]]
  }
}

object ZeppelinRestApi {
  def apply(host: String, port: Int): ZeppelinRestApi = {
    new ZeppelinRestApi(new RestAPI(host, port))
  }
}


