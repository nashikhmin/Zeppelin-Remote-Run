package jetbrains.zeppelin.api.rest

import java.net.HttpCookie

import scalaj.http.{Http, HttpOptions, HttpResponse}
import spray.json._



class RestAPI(host: String, port: Int, https: Boolean = false) {
  private val protocol = if (https) "https" else "http"
  private val apiUrl = s"$protocol://$host:$port/api"


  def performGetRequest(uri: String, cookie: Option[HttpCookie]): HttpResponse[String] = {
    var request = Http(apiUrl + uri)
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))

    cookie.foreach(c => request = request.cookie(c))
    request.asString
  }

  def performPostData(uri: String, data: JsValue = JsObject(), cookie: Option[HttpCookie]): HttpResponse[String] = {
    var request = Http(apiUrl + uri).postData(data.compactPrint)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))

    cookie.foreach(c => request = request.cookie(c))
    val result = request.asString
    result
  }


  def performDeleteData(uri: String, cookie: Option[HttpCookie]): HttpResponse[String] = {
    var request = Http(apiUrl + uri).method("DELETE")
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))

    cookie.foreach(c => request = request.cookie(c))
    val result = request.asString
    result
  }

  def performPostForm(uri: String, params: Map[String, String]): HttpResponse[String] = {
    val paramString = if (params.nonEmpty) "?" + params.map(_.productIterator.mkString("=")).mkString("&")
    val result = Http(s"http://$host:$port/api/login" + paramString).postForm
      .option(HttpOptions.readTimeout(10000)).asString
    result
  }

  def performPutData(uri: String, data: JsValue = JsObject(), cookie: Option[HttpCookie]): HttpResponse[String] = {
    var request = Http(apiUrl + uri).put(data.compactPrint)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000))

    cookie.foreach(c => request = request.cookie(c))
    val result = request.asString
    result
  }
}
