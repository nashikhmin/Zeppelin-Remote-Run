package org.intellij.plugin.zeppelin.models

/**
 * Factory, which create Zeppelin models
 */
object ZeppelinModelFactory {
    @JvmStatic
    fun createNotebook(name: String): Notebook = Notebook("", name)
}