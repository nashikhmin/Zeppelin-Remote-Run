package org.intellij.plugin.zeppelin.api.remote.websocket.rest

import com.github.kittinunf.fuel.core.FuelError
import com.intellij.openapi.diagnostic.Logger
import org.intellij.plugin.zeppelin.models.*
import org.intellij.plugin.zeppelin.utils.JsonParser

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
        val json = JsonParser.toObject(newNotebook)
        val result = try {
            restApi.performPostData("/notebook", json, sessionToken)
        } catch (e: FuelError) {
            throw RestApiException("Cannot create notebook", e.message ?: "")
        }

        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Create notebook' $result")

        val id: String = result.body as String
        return Notebook(id, newNotebook.name)
    }

    fun createParagraph(noteId: String, paragraphText: String): Paragraph {
        val data = mapOf("title" to "", "text" to paragraphText)

        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Create paragraph'. Data: $data")
        val result = try {
            restApi.performPostData("/notebook/$noteId/paragraph", data, sessionToken)
        } catch (e: FuelError) {
            throw RestApiException("Cannot create a paragraph", e.message ?: "")
        }

        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Create paragraph' $result")
        return JsonParser.fromValueObject(result.body, Paragraph::class.java)
    }

    fun deleteNotebook(noteId: String) {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Delete notebook'. NoteId: $noteId")
        val result = try {
            restApi.performDeleteData("/notebook/$noteId", sessionToken)
        } catch (e: FuelError) {
            throw RestApiException("Cannot delete a notebook", e.message ?: "")
        }
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Delete notebook' $result")
    }

    fun deleteParagraph(noteId: String, paragraphId: String) {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Delete paragraph'. NoteId: $noteId")
        val result = try {
            restApi.performDeleteData("/notebook/$noteId/paragraph/$paragraphId",
                    sessionToken)
        } catch (e: FuelError) {
            throw RestApiException("Cannot delete a paragraph", e.message ?: "")
        }
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Delete paragraph' $result")
    }

    fun getInterpreters(): List<Interpreter> {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Get interpreters'")
        val result = try {
            restApi.performGetRequest("/interpreter/setting", sessionToken)
        } catch (e: FuelError) {
            throw RestApiException("Cannot get list of interpreters.",
                    e.message ?: "")
        }

        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Get notebooks' $result")
        return JsonParser.fromValueList(result.body, Interpreter::class.java)
    }

    fun getNotebooks(): List<Notebook> {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Get notebooks'")

        val result = try {
            restApi.performGetRequest("/notebook", sessionToken)
        } catch (e: FuelError) {
            throw RestApiException("Cannot get list of notebooks",
                    e.message ?: "")
        }

        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Get notebooks' $result")

        return JsonParser.fromValueList(result.body, Notebook::class.java)
    }

    fun login(user: User): Credentials {
        if (LOG.isTraceEnabled) LOG.trace("Start perform request 'Login' User:${user.name}")

        val params = mapOf("userName" to user.name, "password" to user.password)

        val (response, result) = try {
            restApi.performPostForm("/login", params)
        } catch (e: FuelError) {
            throw RestApiException("Cannot handle login request.",
                    e.message ?: "")
        }
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Login' Response:$result")

        val headers = response.headers["Set-Cookie"] ?: listOf()
        sessionToken = headers.first { it.contains("JSESSIONID") }.split(";")[0]
        loginStatus = LoginStatus.LOGGED

        return JsonParser.fromValueObject(result.body, Credentials::class.java)
    }

    fun restartInterpreter(interpreter: Interpreter, noteId: String?) {
        if (LOG.isTraceEnabled) {
            LOG.trace("Start perform request 'Restart interpreter' Interpreter:$interpreter, note id: $noteId")
        }
        val data: Map<String, String> = noteId?.let { mapOf("noteId" to noteId) } ?: mapOf()
        val result = try {
            restApi.performPutData("/interpreter/setting/restart/${interpreter.id} ",
                    data, sessionToken)
        } catch (e: FuelError) {
            throw RestApiException("Cannot restart an interpreter.",
                    e.message ?: "")
        }
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Restart interpreter' Response:$result")
    }

    fun updateInterpreterSettings(interpreter: Interpreter) {
        if (LOG.isTraceEnabled) LOG.trace(
                "Start perform request 'Update interpreter settings' Interpreter:$interpreter")
        val data = JsonParser.toObject(interpreter)
        val result = try {
            restApi.performPutData("/interpreter/setting/${interpreter.id} ",
                    data, sessionToken)
        } catch (e: FuelError) {
            throw RestApiException("Cannot update interpreter settings.",
                    e.message ?: "")
        }
        if (LOG.isTraceEnabled) LOG.trace("Performed request 'Update interpreter settings' Response:$result")
    }
}