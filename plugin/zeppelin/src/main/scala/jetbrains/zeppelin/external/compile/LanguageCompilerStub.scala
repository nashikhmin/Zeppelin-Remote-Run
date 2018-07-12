package jetbrains.zeppelin.external.compile

import com.intellij.lang.Language

//TODO: it is just stub and do not using now
class LanguageCompilerStub extends LanguageCompiler {
  override def isSupport(language: Language): Boolean = true
}

object LanguageCompilerStub {
  def apply(): LanguageCompilerStub = new LanguageCompilerStub()
}