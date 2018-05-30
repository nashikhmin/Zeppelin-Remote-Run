package jetbrains.zeppelin.api.rest

class RestApiException(message: String = "", code: Int) extends Exception {
  override def getMessage: String = s"$message.\nError code: $code"
}

object RestApiException {
  def apply(message: String, code: Int): RestApiException = new RestApiException(message, code)
}
