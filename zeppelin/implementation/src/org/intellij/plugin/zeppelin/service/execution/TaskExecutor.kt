package org.intellij.plugin.zeppelin.service.execution

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.intellij.plugin.zeppelin.models.ZeppelinException

/**
 * An executor for one Zeppelin Paragraph Run
 *
 * This class uses coroutines to manage order of input messages and remove duplicates.
 *
 * @param id - an id of an executing paragraph
 * @param outputHandler - a handler which get executions results
 */
class TaskExecutor(val id: String, private val outputHandler: ExecutionHandler) {
    companion object {
        private const val FRESH_RESPONSE_TIME = 1000
        private const val RESPONSE_UPDATE_TIME = 1000L
    }

    private val taskActor = taskActor()

    init {
        delayedResponseHandle()
    }

    fun isCompleted(): Boolean = runBlocking {
        if (taskActor.isClosedForSend) return@runBlocking true
        val responseChannel = Channel<Boolean>()
        taskActor.send(GetIsFinishedMsg(responseChannel))
        responseChannel.receive()
    }

    fun appendOutput(output: OutputResponse) = runBlocking<Unit> {
        val time = System.currentTimeMillis()
        launch {
            taskActor.send(ResponseActorMsg(time, output))
        }
    }

    fun progress(progressResponse: ProgressResponse) = outputHandler.onProgress(progressResponse.progress / 100.0)

    fun paragraph(paragraphResponse: ParagraphResponse) = runBlocking<Unit> {
        val time = System.currentTimeMillis()
        launch {
            taskActor.send(ResponseActorMsg(time, paragraphResponse))
        }
    }

    fun updateOutput(output: OutputResponse) = runBlocking<Unit> {
        val time = System.currentTimeMillis()
        launch {
            taskActor.send(ResponseActorMsg(time, output))
        }
    }

    private fun delayedResponseHandle() = runBlocking<Unit> {
        launch {
            while (!isCompleted()) {
                val responseChannel = Channel<List<ResponseActorMsg>>()
                taskActor.send(GetResponseMsg(responseChannel))
                val received = responseChannel.receive()
                handleResponses(received)
                delay(Companion.RESPONSE_UPDATE_TIME)
            }
            taskActor.close()
            outputHandler.close()
        }
    }

    private suspend fun handleResponses(responses: List<ResponseActorMsg>) {
        if (responses.isEmpty()) return
        val sortedResponses = responses.sortedBy { it.time }.map { it.response }
        val filtered = sortedResponses.zipWithNext().filter { it.first != it.second }
        val responsesCleaned = listOf(sortedResponses.first()) + filtered.map { it.second }

        responsesCleaned.forEach {
            when (it) {
                is ParagraphResponse -> handleParagraphResponse(it)
                is OutputResponse -> outputHandler.onOutput(it, true)
                else -> throw ZeppelinException("Unhandled result")
            }
        }
    }

    private suspend fun handleParagraphResponse(paragraphResponse: ParagraphResponse) {
        val status: String = paragraphResponse.status
        outputHandler.onUpdateExecutionStatus(status)
        when (status) {
            "FINISHED" -> {
                taskActor.send(SendFinishMsg)
                val results: ExecutionResults = paragraphResponse.results?:return
                outputHandler.onSuccess(results)
            }
            "ERROR" -> {
                taskActor.send(SendFinishMsg)
                val results: ExecutionResults = paragraphResponse.results?:return
                outputHandler.onError(results)
            }
        }
    }

    private fun taskActor() = actor<ActorMsg> {
        val responses: MutableList<ResponseActorMsg> = mutableListOf()
        var isFinished = false
        for (msg in channel) { // iterate over incoming messages
            when (msg) {
                is ResponseActorMsg -> responses.add(msg)
                is SendFinishMsg -> isFinished = true
                is GetIsFinishedMsg -> msg.response.send(isFinished && responses.isEmpty())
                is GetResponseMsg -> {
                    val time = System.currentTimeMillis()
                    val (old, fresh) = responses.partition { time - it.time > Companion.FRESH_RESPONSE_TIME }
                    msg.response.send(old.toList())
                    responses.clear()
                    responses.addAll(fresh)
                }
            }
        }
    }
}

private sealed class ActorMsg

private class ResponseActorMsg(val time: Long, val response: ExecutionResponse) : ActorMsg()
private class GetResponseMsg(var response: SendChannel<List<ResponseActorMsg>>) : ActorMsg()

private object SendFinishMsg : ActorMsg()
private class GetIsFinishedMsg(val response: SendChannel<Boolean>) : ActorMsg()