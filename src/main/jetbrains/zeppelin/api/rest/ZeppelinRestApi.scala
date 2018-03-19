package jetbrains.zeppelin.api.rest

import java.net.HttpCookie

import jetbrains.zeppelin.api.ZeppelinAPIProtocol._
import jetbrains.zeppelin.api._
import scalaj.http.HttpResponse
import spray.json._


class ZeppelinRestApi(val restApi: RestAPI) {
  var sessionToken: Option[HttpCookie] = None

  def createNotebook(newNotebook: NewNotebook): Notebook = {
    val result: HttpResponse[String] = restApi.performPostData("/notebook", newNotebook.toJson, sessionToken)
    if (result.code != 201)
      throw RestApiException(s"Cannot create a notebook.\n Error code: ${result.code}.\nBody:${result.body}")

    val id = result.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Notebook(id)
  }


  def createParagraph(noteId: String): Paragraph = {
    val data = Map("title" -> "", "text" -> "%spark\nprintln(\"Paragraph insert revised\")").toJson
    val response: HttpResponse[String] = restApi.performPostData(s"/notebook/$noteId/paragraph", data, sessionToken)

    if (response.code != 201)
      throw RestApiException(s"Cannot create a paragraph.\n Error code: ${response.code}.\nBody:${response.body}")

    val paragraphId = response.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Paragraph(paragraphId)
  }

  def login(userName: String, password: String): Credentials = {
    val result: HttpResponse[String] = restApi.performPostForm("/login", Map("userName" -> userName, "password" -> password))
    if (result.code != 200)
      throw RestApiException(s"Cannot login.\n Error code: ${result.code}.\nBody:${result.body}")
    val json = result.body.parseJson.asJsObject.fields.getOrElse("body", JsObject())
    sessionToken = result.cookies.reverseIterator.find(_.getName == "JSESSIONID")
    json.convertTo[Credentials]
  }
}