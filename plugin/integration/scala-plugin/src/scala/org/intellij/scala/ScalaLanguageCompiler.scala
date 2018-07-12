package org.intellij.scala

import com.intellij.lang.Language
import jetbrains.zeppelin.external.compile.LanguageCompiler
import org.jetbrains.plugins.scala.ScalaLanguage

//TODO: it is just stub and do not using now
class ScalaLanguageCompiler extends LanguageCompiler {
  override def isSupport(language: Language): Boolean = ScalaLanguage.INSTANCE.is(language)
}