package jetbrains.zeppelin.api

import java.net.HttpCookie

import spray.json.DefaultJsonProtocol._
import spray.json._

import scalaj.http.HttpResponse


case class Credentials(principal: String, ticket: String, roles: String)


object CredentialsJsonProtocol extends DefaultJsonProtocol {
  implicit val CredentialsFormat: RootJsonFormat[Credentials] = jsonFormat3(Credentials)
}

case class Notebook(id: String) {}

case class Paragraph(notebookId: String, paragraphId: String) {}


class ZeppelinApi(val restApi: RestAPI) {

  var sessionToken: Option[HttpCookie] = None

  def createNotebook(name: String): Notebook = {
    val data = Map("name" -> name)
    val result: HttpResponse[String] = restApi.performPostData("/notebook", data, sessionToken)

    if (result.code != 201)
      throw RestApiException(s"Cannot create a notebook.\n Error code: ${result.code}.\nBody:${result.body}")

    val id = result.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Notebook(id)
  }


  def createParagraph(noteId: String): Paragraph = {
    val data = Map("title" -> "", "text" -> "%spark\nprintln(\"Paragraph insert revised\")")
    val response: HttpResponse[String] = restApi.performPostData(s"/notebook/$noteId/paragraph", data, sessionToken)

    if (response.code != 201)
      throw RestApiException(s"Cannot create a paragraph.\n Error code: ${response.code}.\nBody:${response.body}")

    val paragraphId = response.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Paragraph(noteId, paragraphId)
  }

  def login(userName: String, password: String): Credentials = {
    val result: HttpResponse[String] = restApi.performPostForm("/login", Map("userName" -> userName, "password" -> password))
    if (result.code != 200)
      throw RestApiException(s"Cannot login.\n Error code: ${result.code}.\nBody:${result.body}")

    import CredentialsJsonProtocol._
    val json = result.body.parseJson.asJsObject.fields.getOrElse("body", JsObject())
    sessionToken = result.cookies.reverseIterator.find(_.getName == "JSESSIONID")
    json.convertTo[Credentials]
  }
}
