package org.intellij.plugin.zeppelin.dependency.dependency

import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor
import org.intellij.plugin.zeppelin.idea.wizard.ZeppelinLibraryDescription

data class LibraryDescriptor(val version: String,
                             val classes: List<String>,
                             val sources: List<String> = listOf(),
                             val docs: List<String> = listOf()) {
    fun createNewLibraryConfiguration(): NewLibraryConfiguration {
        return object : NewLibraryConfiguration(ZeppelinLibraryDescription.LIBRARY_CAPTION + "_" + version) {
            override fun addRoots(editor: LibraryEditor) {
                classes.forEach { it -> editor.addRoot(it, OrderRootType.CLASSES) }
                sources.forEach { it -> editor.addRoot(it, OrderRootType.SOURCES) }
                docs.forEach { it -> editor.addRoot(it, OrderRootType.DOCUMENTATION) }
            }
        }
    }
}