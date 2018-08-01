package jetbrains.zeppelin.wizard


import java.lang.Boolean

import com.intellij.util.ui.{ColumnInfo, ListTableModel}

class SdkTableModel extends ListTableModel[ZeppelinSdkDescriptor](
  new ColumnInfo[ZeppelinSdkDescriptor, String]("Version") {
    override def valueOf(item: ZeppelinSdkDescriptor): String = item.version

    override def getPreferredStringValue = "0.8.0"
  },
  new ColumnInfo[ZeppelinSdkDescriptor, Boolean]("Sources") {
    override def getColumnClass: Class[Boolean] = classOf[java.lang.Boolean]

    override def getPreferredStringValue = "0"

    override def valueOf(item: ZeppelinSdkDescriptor): Boolean = item.sources.nonEmpty
  },
  new ColumnInfo[ZeppelinSdkDescriptor, Boolean]("Docs") {
    override def getColumnClass: Class[Boolean] = classOf[java.lang.Boolean]

    override def getPreferredStringValue = "0"

    override def valueOf(item: ZeppelinSdkDescriptor): Boolean = item.docs.nonEmpty
  })