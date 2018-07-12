package jetbrains.zeppelin.api.rest

case class RestApiException(message: String = "", code: Int) extends Exception {
  override def getMessage: String = s"$message.\nError code: $code"
}