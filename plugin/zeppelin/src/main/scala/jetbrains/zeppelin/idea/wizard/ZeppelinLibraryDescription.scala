package jetbrains.zeppelin.idea.wizard

import java.util

import com.intellij.openapi.roots.libraries.{LibraryKind, NewLibraryConfiguration}
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.JComponent
import jetbrains.zeppelin.dependency.LibraryDescriptor

/**
  * Object which implements methods that add required libraries in Zeppelin module
  */
object ZeppelinLibraryDescription extends CustomLibraryDescription {
  val ZEPPELIN_LIBRARY_KIND: LibraryKind = LibraryKind.create("zeppelin-library")
  val LIBRARY_NAME = "ZeppelinLibrary"
  val JAVA_RUNTIME_LIBRARY_CREATION = "Zeppelin Library Creation"
  val DIALOG_TITLE = "Create Zeppelin Library"
  val LIBRARY_CAPTION = "ZeppelinSDK"

  def createNewLibrary(parentComponent: JComponent, contextDirectory: VirtualFile): NewLibraryConfiguration = {
    val dialog = new ZeppelinSDKSelectionDialog(parentComponent)
    val description = Option[LibraryDescriptor](dialog.open())
    description.map(_.createNewLibraryConfiguration).orNull
  }

  override def getDefaultLevel: LibrariesContainer.LibraryLevel = {
    LibrariesContainer.LibraryLevel.MODULE
  }

  def getSuitableLibraryKinds: util.Set[LibraryKind] = {
    val kinds = new util.HashSet[LibraryKind]()
    kinds.add(ZEPPELIN_LIBRARY_KIND)
    kinds
  }
}