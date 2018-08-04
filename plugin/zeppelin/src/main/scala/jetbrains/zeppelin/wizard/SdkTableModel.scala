package jetbrains.zeppelin.wizard


import java.lang.Boolean

import com.intellij.util.ui.{ColumnInfo, ListTableModel}
import jetbrains.zeppelin.utils.dependency.LibraryDescriptor

class SdkTableModel extends ListTableModel[LibraryDescriptor](
  new ColumnInfo[LibraryDescriptor, String]("Version") {
    override def valueOf(item: LibraryDescriptor): String = item.version

    override def getPreferredStringValue = "0.8.0"
  },
  new ColumnInfo[LibraryDescriptor, Boolean]("Sources") {
    override def getColumnClass: Class[Boolean] = classOf[java.lang.Boolean]

    override def getPreferredStringValue = "0"

    override def valueOf(item: LibraryDescriptor): Boolean = item.sources.nonEmpty
  },
  new ColumnInfo[LibraryDescriptor, Boolean]("Docs") {
    override def getColumnClass: Class[Boolean] = classOf[java.lang.Boolean]

    override def getPreferredStringValue = "0"

    override def valueOf(item: LibraryDescriptor): Boolean = item.docs.nonEmpty
  })