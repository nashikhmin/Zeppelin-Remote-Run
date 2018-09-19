package org.intellij.plugin.zeppelin.api.rest

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpDelete
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.result.Result

open class RestAPI(host: String, port: Int, https: Boolean = false) {
    private val protocol: String = if (https) "https" else "http"
    private val apiUrl: String = "$protocol://$host:$port/api"

    fun performGetRequest(uri: String, credentials: String?): Triple<Request, Response, Result<String, FuelError>> {
        val headers = mapOf("Charset" to "UTF-8").plus(
                credentials?.let { mapOf("Cookie" to "JSESSIONID=$credentials") } ?: emptyMap())
        return "$apiUrl$uri".httpGet()
                .header(headers)
                .timeout(10000)
                .responseString()
    }

    fun performPostData(uri: String, jsonData: String,
                        credentials: String?): Triple<Request, Response, Result<String, FuelError>> {
        val headers = mapOf("Charset" to "UTF-8", "Content-Type" to "application/json").plus(
                credentials?.let { mapOf("Cookie" to "JSESSIONID=$credentials") } ?: emptyMap())

        val mapData = Klaxon().parse<Map<String, Any>>(jsonData) ?: mapOf()
        return "$apiUrl$uri".httpPost(mapData.toList())
                .header(headers)
                .timeout(10000)
                .responseString()
    }

    fun performDeleteData(uri: String, credentials: String?): Triple<Request, Response, Result<String, FuelError>> {
        val headers = mapOf("Charset" to "UTF-8", "Content-Type" to "application/json").plus(
                credentials?.let { mapOf("Cookie" to "JSESSIONID=$credentials") } ?: emptyMap())
        return "$apiUrl$uri".httpDelete()
                .header(headers)
                .timeout(10000)
                .responseString()
    }

    fun performPostForm(uri: String,
                        params: Map<String, String>): Triple<Request, Response, Result<String, FuelError>> {
        val paramString = "?" + params.map { it.key + "=" + it.value }.joinToString("&")
        val headers = mapOf("Charset" to "UTF-8", "Content-Type" to "application/x-www-form-urlencoded")

        return "$apiUrl$uri$paramString".httpPost()
                .header(headers)
                .timeout(10000)
                .responseString()
    }

    fun performPutData(uri: String, jsonData: String,
                       credentials: String?): Triple<Request, Response, Result<String, FuelError>> {
        val headers = mapOf("Charset" to "UTF-8", "Content-Type" to "application/json").plus(
                credentials?.let { mapOf("Cookie" to "JSESSIONID=$credentials") } ?: emptyMap())
        val mapData = Klaxon().parse<Map<String, Any>>(jsonData) ?: mapOf()

        return "$apiUrl$uri".httpPut(mapData.toList())
                .header(headers)
                .timeout(10000)
                .responseString()
    }
}