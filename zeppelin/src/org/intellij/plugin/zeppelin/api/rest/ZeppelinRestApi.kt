package org.intellij.plugin.zeppelin.api.rest

import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon

import com.intellij.openapi.diagnostic.Logger
import org.intellij.plugin.zeppelin.models.*

/**
 * The service to work with Zeppelin by the RESt API
 *
 * @param restApi - REST service
 */
class ZeppelinRestApi(private val restApi: RestAPI) {
    private val LOG: Logger = Logger.getInstance(ZeppelinRestApi::class.java)
    private var sessionToken: String? = null
    var loginStatus: LoginStatus = LoginStatus.NOT_LOGGED

    fun createNotebook(newNotebook: NewNotebook): Notebook {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Create notebook $newNotebook'")
        val json = Klaxon().toJsonString(newNotebook)
        val (_, response, result) = restApi.performPostData("/notebook", json, sessionToken)

        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Create notebook' $response")
        if (response.statusCode != 200) {
            throw RestApiException("Cannot create notebook.", response.statusCode)
        }
        val responseMessage = response.responseMessage
        val id: String = Klaxon().parse<Notebook>(responseMessage)?.id ?: throw ParseException(responseMessage,
                Notebook::class.toString())
        return Notebook(id, newNotebook.name)
    }

    fun createParagraph(noteId: String, paragraphText: String): Paragraph {
        val data = mapOf("title" to "", "text" to paragraphText)
        val json = Klaxon().toJsonString(data)
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Create paragraph'. Data: $data")
        val (_, response, result) = restApi.performPostData("/notebook/$noteId/paragraph", json, sessionToken)
        if (response.statusCode != 200) {
            throw RestApiException("Cannot create a paragraph", response.statusCode)
        }
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Create paragraph' $response")

        val responseMessage = response.responseMessage
        return Klaxon().parse<Paragraph>(responseMessage)
                ?: throw ParseException(responseMessage, Paragraph::class.toString())
    }

    fun deleteNotebook(noteId: String) {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Delete notebook'. NoteId: $noteId")
        val (_, response, result) = restApi.performDeleteData("/notebook/$noteId", sessionToken)
        if (response.statusCode != 200) {
            throw RestApiException("Cannot delete a paragraph", response.statusCode)
        }
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Delete notebook' $response")
    }

    fun deleteParagraph(noteId: String, paragraphId: String) {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Delete paragraph'. NoteId: $noteId")
        val (_, response, result) = restApi.performDeleteData("/notebook/$noteId/paragraph/$paragraphId",
                sessionToken)
        if (response.statusCode != 200) throw RestApiException("Cannot delete a paragraph", response.statusCode)
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Delete paragraph' $response")
    }

    fun getInterpreters(): List<Interpreter> {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Get interpreters'")
        val (_, response, result) = restApi.performGetRequest("/interpreter/setting", sessionToken)
        if (response.statusCode != 200) throw RestApiException("Cannot get list of interpreters.", response.statusCode)
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Get interpreters' $response")
        val responseMessage = response.responseMessage
        return Klaxon().parse<List<Interpreter>>(responseMessage)
                ?: throw ParseException(responseMessage, List::class.toString())
    }

    fun getNotebooks(prefix: String=""): List<Notebook> {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Get notebooks'")
        val (_, response, result) = restApi.performGetRequest("/notebook", sessionToken)
        if (response.statusCode != 200) throw RestApiException("Cannot get list of notebooks", response.statusCode)
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Get notebooks' $response")
        val responseMessage = response.responseMessage
        return Klaxon().parse<List<Notebook>>(responseMessage)
                ?: throw ParseException(responseMessage, Notebook::class.toString())
    }

    data class ResponseMessage(val status:String, val message:String,val body: JsonValue)

    fun login(userName: String, password: String): Credentials {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Login' User:$userName")
        val params = mapOf("userName" to userName,
                "password" to password)
        val (_, response, result) = restApi.performPostForm("/login", params)
        val parse = Klaxon().parse<ResponseMessage>(result.get())
        if (response.statusCode != 200) {
            throw RestApiException("Cannot login.", response.statusCode)
        }

        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Login' Response:$response")
        val responseMessage = response.responseMessage

        val cookie = response.headers["Set-Cookie"]?.reversed()
        val sessionString = cookie?.first { it.contains("JSESSIONID") }
        val sessionPart = sessionString?.split(";")?.first()
        sessionToken = sessionPart?.split("=")?.get(1)
        loginStatus = LoginStatus.LOGGED

        return Klaxon().parse<Credentials>(responseMessage)
                ?: throw ParseException(responseMessage, Notebook::class.toString())
    }

    fun restartInterpreter(interpreter: Interpreter, noteId: String?) {
        if (LOG.isTraceEnabled) {
            LOG.trace("Start perform request 'Restart interpreter' Interpreter:$interpreter, note id: $noteId")
        }
        val data: Map<String, String> = noteId?.let { mapOf("noteId" to noteId) } ?: mapOf()
        val json = Klaxon().toJsonString(data)
        val (_, response, result) = restApi.performPutData("/interpreter/setting/restart/${interpreter.id} ",
                json, sessionToken)
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Restart interpreter' Response:$response")
        if (response.statusCode != 200) {
            throw RestApiException("Cannot interpreter settings.", response.statusCode)
        }
    }

    fun updateInterpreterSettings(interpreter: Interpreter) {
        if (LOG.isTraceEnabled) LOG.trace(
                "Start perform request 'Update interpreter settings' Interpreter:$interpreter")
        val data: String = Klaxon().toJsonString(interpreter)
        val (_, response, result) = restApi.performPutData("/interpreter/setting/${interpreter.id} ",
                data, sessionToken)
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Update interpreter settings' Response:$response")
        if (response.statusCode != 200) {
            throw RestApiException("Cannot interpreter settings.", response.statusCode)
        }
    }
}