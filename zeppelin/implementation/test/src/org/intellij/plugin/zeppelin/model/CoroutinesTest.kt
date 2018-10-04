/*
 * Copyright 2016-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */


import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.util.*


//TODO: THIS CLASS IS TEMP, DELETE IT!!!

// Message types for counterActor
sealed class CounterMsg

class InCounter(val i: Int, val time: Long) : CounterMsg() // one-way message to increment counter
class GetCounter(var response: SendChannel<List<Pair<Int,Long>>>) : CounterMsg() // a request with reply

// This function launches a new counter actor
fun counterActor() = actor<CounterMsg> {
    val results: MutableList<Pair<Int,Long>> = mutableListOf()
    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is InCounter -> results.add(msg.i to msg.time)
            is GetCounter -> {
                val time = System.currentTimeMillis()
                val (old,fresh) = results.partition { time-it.second>1000 }

                msg.response.send(old.sortedBy { it.first})
                results.clear()
                results.addAll(fresh)
            }
        }
    }
}

fun main1(args: Array<String>) = runBlocking<Unit> {
    val counter = counterActor() // create the actor

    val job1 = launch {
        var i = 0
        repeat(20) {
            val randTimeWait = Random().nextInt(500)
            delay(randTimeWait)
            val time = System.currentTimeMillis()
            counter.send(InCounter(i * 2, time))
            i++
        }
    }
    val job2 = launch {
        var i = 0
        repeat(20) {
            val randTimeWait = Random().nextInt(500)
            delay(randTimeWait)
            val time = System.currentTimeMillis()
            counter.send(InCounter(i * 2 + 1, time))
            i++
        }
    }


    launch {
        while (true) {
            delay(2000)
            val response = Channel<List<Pair<Int,Long>>>()
            counter.send(GetCounter(response))
            val receive = response.receive()
            println("Counter = ${receive.map { it.first }}")
        }

    }
    job1.join()
    job2.join()
    counter.close() // shutdown the actor
}


class A {
    @Test
    fun test01 () {
        val a = listOf(1,2,2,3,3,3,4,4,5)
        val filtered = a.zipWithNext().filter { it.first != it.second }
        val res = filtered.map { it.first } + filtered.last().second
        println(res)
    }
}
