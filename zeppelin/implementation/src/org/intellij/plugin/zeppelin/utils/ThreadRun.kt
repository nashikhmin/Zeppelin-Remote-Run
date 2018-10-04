package org.intellij.plugin.zeppelin.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.*
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project

fun <T> invokeLater(body: () -> T) {
    ApplicationManager.getApplication().invokeLater { body() }
}

fun <T> withProgressSynchronously(title: String, body: () -> T): T? {
    val manager: ProgressManager = ProgressManager.getInstance()
    return try {
        return manager.runProcessWithProgressSynchronously<T, Exception>(body, title, false, null)
    } catch (e: Exception) {
        null
    }
}

fun createBgIndicator(project: Project, name: String): ProgressIndicator {
    return ProgressIndicatorProvider.getGlobalProgressIndicator() ?: BackgroundableProcessIndicator(
            project, name, PerformInBackgroundOption.ALWAYS_BACKGROUND, null, null, false)
}