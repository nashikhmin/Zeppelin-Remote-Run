package org.intellij.plugin.zeppelin.models


class ZeppelinException() extends Exception {
  override def getMessage: String = {
    s"Error during Zeppelin Exception."
  }
}

case class ZeppelinConnectionException(uri: String) extends ZeppelinException {
  override def getMessage: String = {
    s"Cannot connect to the Zeppelin app. " +
      s"Check the availability of web socket connection to the service $uri"
  }
}

case class ZeppelinLoginException() extends ZeppelinException {
  override def getMessage: String = {
    s"Cannot login to the Zeppelin app. " +
      s"The login or the password is wrong."
  }
}


case class NotebookNotFoundException(id: String) extends ZeppelinException {
  override def getMessage: String = {
    s"Notebook with $id id is not found."
  }
}