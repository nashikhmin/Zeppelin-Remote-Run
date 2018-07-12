package jetbrains.zeppelin.external.compile

import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName

//TODO: it is just stub and do not using now
trait LanguageCompiler {
  def isSupport(language: Language): Boolean
}

object LanguageCompiler {
  val EP_NAME: ExtensionPointName[LanguageCompiler] = ExtensionPointName
    .create("org.jetbrains.scala.zeppelin.compileProject")
}