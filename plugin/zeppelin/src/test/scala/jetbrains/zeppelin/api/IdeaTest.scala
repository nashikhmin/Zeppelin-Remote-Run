package jetbrains.zeppelin.api

import jetbrains.zeppelin.service.SbtCompiler


class IdeaTest extends AbstractScalaTest {
  test("Zeppelin.RunPackage") {
    val jar = SbtCompiler().compileAndPackage("../..")
    assert(jar.endsWith(".jar"))
  }
}
