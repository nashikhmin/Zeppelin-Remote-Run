package org.intellij.plugin.zeppelin.service.execution

import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.intellij.plugin.zeppelin.models.ZeppelinException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An executor for one Zeppelin Paragraph Run
 *
 * @param id - an id of an executing paragraph
 * @param outputHandler - a handler which get executions results
 */
class TaskExecutor(val id: String, private val outputHandler: ExecutionHandler) {
    private val taskActor = taskActor()

    init {
        delayedResponseHandle()
    }

    fun isCompleted(): AtomicBoolean = outputHandler.isCompleted()

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
            while (!isCompleted().get()) {
                val responseChannel = Channel<List<ResponseActorMsg>>()
                taskActor.send(GetResponseMsg(responseChannel))
                val received = responseChannel.receive()
                handleResponses(received)
                delay(1000L)
            }
            taskActor.close()
        }
    }

    private fun handleResponses(responses: List<ResponseActorMsg>) {
        if (responses.isEmpty()) return
        val sortedResponses = responses.sortedBy { it.time }.map { it.response }
        val filtered = sortedResponses.zipWithNext().filter { it.first != it.second }
        val responsesCleaned = listOf(sortedResponses.first()) + filtered.map { it.second }

        //TODO: this module should to say when the process is eng GUI is just dummy dependent output handler
        responsesCleaned.forEach {
            when (it) {
                is ParagraphResponse -> handleParagraphResponse(it)
                is OutputResponse -> outputHandler.onOutput(it, true)
                else -> throw ZeppelinException("Unhandled result")
            }
        }
    }

    private fun handleParagraphResponse(
            paragraphResponse: ParagraphResponse) {
        val status: String = paragraphResponse.status
        outputHandler.onUpdateExecutionStatus(status)
        when (status) {
            "FINISHED" -> {
                val results: ExecutionResults = paragraphResponse.results!!
                outputHandler.onSuccess(results)
            }
            "ERROR" -> {
                val results: ExecutionResults = paragraphResponse.results!!
                outputHandler.onError(results)
            }
        }
    }

    private fun taskActor() = actor<ActorMsg> {
        val results: MutableList<ResponseActorMsg> = mutableListOf()
        for (msg in channel) { // iterate over incoming messages
            when (msg) {
                is ResponseActorMsg -> results.add(msg)
                is GetResponseMsg -> {
                    val time = System.currentTimeMillis()
                    val (old, fresh) = results.partition { time - it.time > 1000 }
                    msg.response.send(old.toList())
                    results.clear()
                    results.addAll(fresh)
                }
            }
        }
    }
}

private sealed class ActorMsg

private class ResponseActorMsg(val time: Long, val response: ExecutionResponse) : ActorMsg()

private class GetResponseMsg(var response: SendChannel<List<ResponseActorMsg>>) : ActorMsg()
