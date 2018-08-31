package org.intellij.plugin.zeppelin.idea.wizard

import java.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.libraries.{LibraryKind, NewLibraryConfiguration}
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.JComponent
import org.intellij.plugin.zeppelin.dependency.LibraryDescriptor
import org.intellij.plugin.zeppelin.idea.wizard.ZeppelinLibraryDescription._

/**
  * Object which implements methods that add required libraries in Zeppelin module
  */
class ZeppelinLibraryDescription(project: Project) extends CustomLibraryDescription {

  def createNewLibrary(parentComponent: JComponent, contextDirectory: VirtualFile): NewLibraryConfiguration = {
    val dialog = new ZeppelinSDKSelectionDialog(parentComponent, project)
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

object ZeppelinLibraryDescription {
  val ZEPPELIN_LIBRARY_KIND: LibraryKind = LibraryKind.create("zeppelin-library")
  val LIBRARY_NAME = "ZeppelinLibrary"
  val JAVA_RUNTIME_LIBRARY_CREATION = "Zeppelin Library Creation"
  val DIALOG_TITLE = "Create Zeppelin Library"
  val LIBRARY_CAPTION = "ZeppelinSDK"

  def apply(project: Project): ZeppelinLibraryDescription = new ZeppelinLibraryDescription(project)
}