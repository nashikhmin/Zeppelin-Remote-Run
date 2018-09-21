package org.intellij.plugin.zeppelin.api.remote.websocket.rest

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.moshi.responseObject
import com.github.kittinunf.result.Result
import org.intellij.plugin.zeppelin.model.RestResponseMessage
import org.intellij.plugin.zeppelin.utils.JsonParser

open class RestAPI(host: String, port: Int, https: Boolean = false) {
    private val protocol: String = if (https) "https" else "http"
    private val apiUrl: String = "$protocol://$host:$port/api"

    fun performGetRequest(uri: String,
                          credentials: String?): RestResponseMessage {
        val headers = credentials?.let { mapOf("Cookie" to credentials) } ?: emptyMap()
        val (_, _, result) = "$apiUrl$uri".httpGet()
                .header(headers)
                .timeout(10000)
                .responseObject<RestResponseMessage>()
        return getResponse(result)
    }

    fun performPostData(uri: String, data: Map<String, Any>, credentials: String?): RestResponseMessage {
        val headers = mapOf("Charset" to "UTF-8", "Content-Type" to "application/json").plus(
                credentials?.let { mapOf("Cookie" to credentials) } ?: emptyMap())

        val (_, _, result) = "$apiUrl$uri".httpPost()
                .header(headers)
                .body(JsonParser.toJson(data))
                .timeout(10000)
                .responseObject<RestResponseMessage>()
        return getResponse(result)
    }

    fun performDeleteData(uri: String, credentials: String?): RestResponseMessage {
        val headers = mapOf("Charset" to "UTF-8", "Content-Type" to "application/json").plus(
                credentials?.let { mapOf("Cookie" to credentials) } ?: emptyMap())
        val (_, _, result) = "$apiUrl$uri".httpDelete()
                .header(headers)
                .timeout(10000)
                .responseObject<RestResponseMessage>()
        return getResponse(result)
    }

    fun performPostForm(uri: String, params: Map<String, String>): Pair<Response, RestResponseMessage> {
        val paramString = "?" + params.map { it.key + "=" + it.value }.joinToString("&")
        val headers = mapOf("Charset" to "UTF-8", "Content-Type" to "application/x-www-form-urlencoded")

        val (_, response, result) = "$apiUrl$uri$paramString".httpPost()
                .header(headers)
                .timeout(10000)
                .responseObject<RestResponseMessage>()
        return Pair(response,getResponse(result))
    }

    fun performPutData(uri: String, data: Map<String, Any>,
                       credentials: String?): RestResponseMessage {
        val headers = mapOf("Charset" to "UTF-8", "Content-Type" to "application/json").plus(
                credentials?.let { mapOf("Cookie" to credentials) } ?: emptyMap())

        val (_, _, result) = "$apiUrl$uri".httpPut()
                .header(headers)
                .body(JsonParser.toJson(data))
                .timeout(10000)
                .responseObject<RestResponseMessage>()
        return getResponse(result)    }

    private fun getResponse(
            result: Result<RestResponseMessage, FuelError>): RestResponseMessage {
        val (obj, errors) = result
        if (errors != null) {
            throw errors
        }
        return obj!!
    }
}