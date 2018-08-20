package org.jetbrains.zeppelin.dataviz

import jetbrains.zeppelin.service.TableOutputHandler
import jetbrains.zeppelin.utils.ZeppelinLogger
import org.intellij.datavis.data.DataProvider

class DatavizOutputHandler extends TableOutputHandler {
  override def invoke(msg: String): Unit = {
    val id = "zeppelin-result"
    val name = "result"
    val dataProvider: DataProvider = DataProvider.INSTANCE
    try {
      if (dataProvider.isExist(id)) {
        dataProvider.removeData(id)
      }
      dataProvider.addData(id, name, msg, '\t')
    }
    catch {
      case e: Throwable => {
        ZeppelinLogger.printError(s"Cannot perform show table in dataviz plugin. ${e.getMessage}")
      }
    }
  }
}
