package org.intellij.plugin.zeppelin.utils

import org.intellij.plugin.zeppelin.AbstractScalaTest
import org.intellij.plugin.zeppelin.models.{InstantiationType, InterpreterOption}

class ModelTests extends AbstractScalaTest {
  test("zeppelin.model.interpreter.option") {
    val option = InterpreterOption(Some(InstantiationType.ISOLATED.toString), Some(InstantiationType.SCOPED.toString))
    assert(option.perNoteAsEnum == InstantiationType.ISOLATED)
    assert(option.perUserAsEnum == InstantiationType.SCOPED)
    assert(option.perNoteAsString == InstantiationType.ISOLATED.toString)
    assert(option.perUserAsString == InstantiationType.SCOPED.toString)
  }
}