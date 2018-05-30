package jetbrains.zeppelin.service

import java.nio.charset.Charset

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.{OSProcessHandler, ProcessEvent, ProcessListener}
import com.intellij.openapi.util.Key
import jetbrains.zeppelin.utils.ZeppelinLogger

class SbtService {
  private val defaultListener = new ProcessListener {
    override def startNotified(processEvent: ProcessEvent): Unit = {}

    override def processTerminated(processEvent: ProcessEvent): Unit = {}

    override def processWillTerminate(processEvent: ProcessEvent,
                                      b: Boolean): Unit = {}

    override def onTextAvailable(processEvent: ProcessEvent,
                                 key: Key[_]): Unit = {
      println(processEvent.getText)
    }
  }
  private val packageListener: PackageTextListener = new PackageTextListener

  def packageToJarCurrentProject(path: String): String = {
    ZeppelinLogger.printMessage("Clean the project...")
    runSyncShellCommand(List("sbt", "clean"), path)
    ZeppelinLogger.printMessage("The project has been cleaned")
    ZeppelinLogger.printMessage("Create a package...")
    runSyncShellCommand(List("sbt", "package"), path, packageListener)
    ZeppelinLogger.printMessage("The package is created")
    packageListener.jar
  }

  private def runSyncShellCommand(commands: List[String], path: String,
                                  listener: ProcessListener = defaultListener) = {
    import scala.collection.JavaConverters._
    val generalCommandLine = new GeneralCommandLine(commands.asJava)
    generalCommandLine.setWorkDirectory(path)
    generalCommandLine.setCharset(Charset.forName("UTF-8"))

    val processHandler = new OSProcessHandler(generalCommandLine)
    processHandler.addProcessListener(listener)
    processHandler.startNotify()
    processHandler.waitFor()
    processHandler
  }

  class PackageTextListener extends ProcessListener {
    private val escapeCodes =
      Seq(Console.RESET,
        Console.RED,
        Console.GREEN,
        Console.BLUE,
        Console.YELLOW)
    var jarFile = ""

    def jar: String = jarFile

    override def startNotified(processEvent: ProcessEvent): Unit = {}

    override def processTerminated(processEvent: ProcessEvent): Unit = {
      println(processEvent.getText)
    }

    override def processWillTerminate(processEvent: ProcessEvent,
                                      b: Boolean): Unit = {}

    override def onTextAvailable(processEvent: ProcessEvent,
                                 outputType: Key[_]): Unit = {
      val text = processEvent.getText
      val escapedText = escape(text)
      if (escapedText.startsWith("[info] Packaging "))
        jarFile = escapedText.split(" ")(2)
    }

    private def escape(text: String): String = {
      escapeCodes.fold(text)((t, c) => t.replaceAllLiterally(c, ""))
    }
  }

}

object SbtService {
  def apply(): SbtService = new SbtService()
}

