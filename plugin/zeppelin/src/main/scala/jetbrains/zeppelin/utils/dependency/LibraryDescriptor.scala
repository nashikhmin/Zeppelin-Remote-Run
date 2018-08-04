package jetbrains.zeppelin.utils.dependency

import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor
import jetbrains.zeppelin.wizard.ZeppelinLibraryDescription.LIBRARY_CAPTION

case class LibraryDescriptor(version: String,
                             classes: List[String],
                             sources: List[String] = List(),
                             docs: List[String] = List()) {
  def createNewLibraryConfiguration: NewLibraryConfiguration = {
    new NewLibraryConfiguration(LIBRARY_CAPTION + "_" + version) {
      override def addRoots(editor: LibraryEditor): Unit = {
        classes.foreach(it => {
          editor.addRoot(it, OrderRootType.CLASSES)
        })

        sources.foreach(it => {
          editor.addRoot(it, OrderRootType.SOURCES)
        })

        docs.foreach(it => {
          editor.addRoot(it, OrderRootType.DOCUMENTATION)
        })
      }
    }
  }
}