package jetbrains.zeppelin.api

import spray.json.DefaultJsonProtocol._
import spray.json._

import scalaj.http.{Http, HttpOptions, HttpResponse}


final case class RestApiException(private val message: String = "",
                                  private val cause: Throwable = None.orNull) extends Exception(message, cause)

class RestAPI(host: String, port: Int, https: Boolean = false) {
  private val protocol = if (https) "https" else "http"
  private val apiUrl = s"$protocol://$host:$port/api"
  private val sessionId = ""


  def performPost(uri: String, data: Map[String, String]): HttpResponse[String] = {
    val result = Http(apiUrl + uri).postData(data.toJson.toString())
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
    result
  }
}
