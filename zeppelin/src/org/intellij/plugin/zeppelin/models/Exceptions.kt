package org.intellij.plugin.zeppelin.models

open class ZeppelinException(val msg:String?=null) : Exception() {
    override val message: String
        get() = "Error in Zeppelin plugin. ${msg?:""}"
}

data class ZeppelinConnectionException(val uri: String) : ZeppelinException() {
    override val message: String
        get() = "Cannot connect to the Zeppelin app. Check the availability of web socket connection to the service $uri"
}

class ZeppelinLoginException : ZeppelinException() {
    override val message: String
        get() = "Cannot login to the Zeppelin app. The login or the password is wrong."
}

data class NotebookNotFoundException(val id: String) : ZeppelinException() {
    override val message: String
        get() = "Notebook with $id id is not found."
}

data class ParagraphNotFoundException(val executeContext: ExecuteContext) : ZeppelinException() {
    override val message: String
        get() = "Paragraph with is not found. Data: $executeContext"
}

class SessionIsClosedException : ZeppelinException()

class ParseException(private val json: String,
                     private val parseClass: String) : ZeppelinException() {
    override val message: String
        get() = "Cannot parse json to object.\nJson:\n$json\nClass:\n$parseClass"

}