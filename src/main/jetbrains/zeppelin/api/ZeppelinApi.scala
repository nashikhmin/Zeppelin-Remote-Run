package jetbrains.zeppelin.api

import spray.json.DefaultJsonProtocol._
import spray.json._

import scalaj.http.HttpResponse

case class Notebook(id: String)

case class Paragraph(notebookId: String, paragraphId: String)

class ZeppelinApi(val restApi: RestAPI) {
  def createNotebook(name: String): Notebook = {
    val data = Map("name" -> name)
    val result: HttpResponse[String] = restApi.performPost("/notebook", data)

    if (result.code != 201)
      throw RestApiException(s"Cannot create a notebook.\n Error code: ${result.code}.\nBody:${result.body}")

    val id = result.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Notebook(id)
  }


  def createParagraph(noteId: String): Paragraph = {
    val data = Map("title" -> "", "text" -> "%spark\nprintln(\"Paragraph insert revised\")")
    val result: HttpResponse[String] = restApi.performPost(s"/notebook/$noteId/paragraph", data)

    if (result.code != 201)
      throw RestApiException(s"Cannot create a paragraph.\n Error code: ${result.code}.\nBody:${result.body}")

    val paragraphId = result.body.parseJson.convertTo[Map[String, String]].getOrElse("body", "")
    Paragraph(noteId, paragraphId)
  }
}
