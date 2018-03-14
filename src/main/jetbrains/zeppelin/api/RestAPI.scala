package jetbrains.zeppelin.api

import java.net.HttpCookie

import spray.json.DefaultJsonProtocol._
import spray.json._

import scalaj.http.{Http, HttpOptions, HttpResponse}


final case class RestApiException(private val message: String = "",
                                  private val cause: Throwable = None.orNull) extends Exception(message, cause)

class RestAPI(host: String, port: Int, https: Boolean = false) {
  private val protocol = if (https) "https" else "http"
  private val apiUrl = s"$protocol://$host:$port/api"


  def performPostData(uri: String, data: Map[String, String] = Map(), cookie: Option[HttpCookie]): HttpResponse[String] = {
    var request = Http(apiUrl + uri).postData(data.toJson.toString())
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))

    if (cookie.isDefined) {
      request = request.cookie(cookie.get)
    }
    //cookie.fold(request)(cookie => request.header("Cookie", s"${cookie.getName}=${cookie.getValue}"))

    val result = request.asString
    result
  }

  def performPostForm(uri: String, params: Map[String, String]): HttpResponse[String] = {
    val paramString = if (params.nonEmpty) "?" + params.map(_.productIterator.mkString("=")).mkString("&")
    val result = Http("http://localhost:8080/api/login" + paramString).postForm
      .option(HttpOptions.readTimeout(10000)).asString
    result
  }
}
