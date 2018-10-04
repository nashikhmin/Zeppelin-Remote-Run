package org.intellij.plugin.zeppelin.idea.wizard

import com.intellij.openapi.roots.libraries.LibraryKind
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer.LibraryLevel
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.plugin.zeppelin.dependency.LibraryDescriptor
import javax.swing.JComponent

/**
 * Object which implements methods that add required libraries in Zeppelin module
 */
class ZeppelinLibraryDescription : CustomLibraryDescription() {

    override fun createNewLibrary(parentComponent: JComponent,
                                  contextDirectory: VirtualFile?): NewLibraryConfiguration? {
        val dialog = ZeppelinSDKSelectionDialog(parentComponent)
        val description: LibraryDescriptor? = dialog.open()
        return description?.createNewLibraryConfiguration()
    }

    override fun getDefaultLevel(): LibraryLevel {
        return LibrariesContainer.LibraryLevel.MODULE
    }

    override fun getSuitableLibraryKinds(): Set<LibraryKind> {
        val kinds: HashSet<LibraryKind> = HashSet()
        kinds.add(ZEPPELIN_LIBRARY_KIND)
        return kinds
    }

    companion object {
        val ZEPPELIN_LIBRARY_KIND: LibraryKind = LibraryKind.create("zeppelin-library")
        const val LIBRARY_CAPTION: String = "ZeppelinSDK"
    }
}