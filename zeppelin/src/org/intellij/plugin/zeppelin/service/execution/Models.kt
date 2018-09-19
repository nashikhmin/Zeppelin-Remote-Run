package org.intellij.plugin.zeppelin.service.execution

import com.beust.klaxon.Klaxon

data class ProgressResponse(val id: String, val progress: Int)

data class ParagraphResponse(val id: String, val status: String, val results: ExecutionResults?)

data class ParagraphAppendResponse(val id: String)

data class ParagraphUpdateOutputResponse(val data: String, val noteId: String, val paragraphId: String, val index: Int)

data class ExecutionResults(val code: String, val msg: List<ExecutionResultsMsg>)

data class ExecutionResultsMsg(val resultType: String, val data: String)

data class OutputResponse(val data: String, val index: Int, val noteId: String, val paragraphId: String)

object ExecutionModelConverter {
    private val klaxon = Klaxon()

    fun getOutputResult(json: String): OutputResponse {
        return klaxon.parse<OutputResponse>(json)!!
    }

    fun getParagraphResponse(json: String): ParagraphResponse {
        return klaxon.parse<ParagraphResponse>(json)!!
    }

    fun getProgressResponse(json: String): ProgressResponse {
        return klaxon.parse<ProgressResponse>(json)!!
    }
}