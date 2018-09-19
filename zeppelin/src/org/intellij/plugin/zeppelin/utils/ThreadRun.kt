package org.intellij.plugin.zeppelin.utils

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.util.Processor
import convertedFromScala.lib.MatchError
import java.util.concurrent.Callable
import java.util.concurrent.Executors


fun <T> invokeLater(body: () -> T): Unit {
    ApplicationManager.getApplication().invokeLater { body() }
}

fun <T> withProgressSynchronously(title: String, body: () -> T): T? {
    val manager: ProgressManager = ProgressManager.getInstance()
    return try {
        return manager.runProcessWithProgressSynchronously<T,Exception>(body,title,false,null)

    } catch (e: Exception) {
        null
    }
}

fun createBgIndicator(project: Project, name: String): ProgressIndicator {
    return ProgressIndicatorProvider.getGlobalProgressIndicator()?:BackgroundableProcessIndicator(
            project, name, PerformInBackgroundOption.ALWAYS_BACKGROUND, null, null, false)
}
object ThreadRun {
//    fun <T> toCallable(action: () -> T): Callable<T> = return { action() }
//    fun <T> toComputable(action: () -> T): Computable<T> = return { action() }
//    fun <A, B> toIdeaFunction(f: Any): Function<A, B> = return { param -> f(param) }
//    fun <T> toProcessor(action: Any): Processor<T> = return { t -> action(t) }
//    fun toRunnable(action: () -> Any): Runnable = return { action() }
//    internal val DEFAULT_MAX_EXECUTION_TIME: Int = 120

//
//    fun <T> inReadAction(body: Computable<T>): T {
//        val application = ApplicationManager.getApplication()
//        if (application.isReadAccessAllowed) return body.compute()
//
//        runReadAction {
//
//        }
//        return application.runReadAction(body)
//    }
//
//    fun <T> inWriteAction(body: () -> T): T {
//        val application: Application = ApplicationManager.getApplication()
//        return if (application.isWriteAccessAllowed) {
//            return body()
//        } else {
//            return body.handle()
//      )`*/
//        }
//    }

//
//    fun <T> runWithTimeout(f: () -> T): Unit {
//        val context: Any = /* ERROR converting `ExecutionContext.fromExecutor`*/Executors.newSingleThreadExecutor()
//        /* ERROR converting `Await.result`*//* ERROR converting `Future`*/f(), /* ERROR converting `Duration`*/(ThreadRun.DEFAULT_MAX_EXECUTION_TIME, TimeUnit.SECONDS))
//    }
//
//    fun <T> withProgressSynchronously(title: String, body: () -> T): T {
//        return run {
//            val match = /* ERROR converting `ThreadRun.withProgressSynchronouslyTry()[T]`*/title { _ -> body() }
//
//            data class `Success(result)_data`(public val result: Any)
//            data class `Failure(exception)_data`(public val exception: Any)
//
//            val `Success(result)` by lazy {
//                if (match is) {
//                    val (result) = match
//                    run {
//                        return@lazy `Success(result)_data`(result)
//                    }
//                }
//                return@lazy null
//            }
//            val `Failure(exception)` by lazy {
//                if (match is) {
//                    val (exception) = match
//                    run {
//                        return@lazy `Failure(exception)_data`(exception)
//                    }
//                }
//                return@lazy null
//            }
//            when {
//                `Success(result)` != null -> {
//                    val (result) = `Success(result)`
//                    return result
//                }
//                `Failure(exception)` != null -> {
//                    val (exception) = `Failure(exception)`
//                    return /* ERROR converting `throw exception`*/
//                }
//                else -> throw MatchError(match)
//            }
//        }
//    }
//
//    fun <T> withProgressSynchronouslyTry(title: String, body: Any): Any {
//        val manager: ProgressManager = ProgressManager.getInstance()
//        return /* ERROR converting `catching(classOf[Exception]).withTry`*/(/* ERROR converting `manager.runProcessWithProgressSynchronously(new ThrowableComputable[T, Exception] {
//      def compute: T = return body(manager)
//    }, title, false, null)`*/)
//    }
}