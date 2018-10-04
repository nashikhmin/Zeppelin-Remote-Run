package org.intellij.plugin.zeppelin.service.execution

import org.intellij.plugin.zeppelin.models.ZeppelinException
import org.intellij.plugin.zeppelin.utils.JsonParser

data class ProgressResponse(val id: String, val progress: Int)

data class ParagraphResponse(val id: String, val status: String, val results: ExecutionResults?)

data class ParagraphAppendResponse(val id: String)

data class ParagraphUpdateOutputResponse(val data: String, val noteId: String, val paragraphId: String, val index: Int)

data class ExecutionResults(val code: String, val msg: List<ExecutionResultsMsg>)

data class ExecutionResultsMsg(val type: String="", val data: String)

data class OutputResponse(val data: String, val index: Int, val noteId: String, val paragraphId: String)

object ExecutionModelConverter {
    fun getOutputResult(json: Any): OutputResponse {
        return JsonParser.fromValueObject(json, OutputResponse::class.java)
    }

    fun getParagraphResponse(json: Any): ParagraphResponse {
        val map = JsonParser.fromValueMap(json,ParagraphResponse::class.java)
        return map["paragraph"]?: throw ZeppelinException("Cannot parse web socket paragraph response")
    }

    fun getProgressResponse(json: Any): ProgressResponse {
        return JsonParser.fromValueObject(json, ProgressResponse::class.java)
    }
}