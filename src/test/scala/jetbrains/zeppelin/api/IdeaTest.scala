package jetbrains.zeppelin.api

import jetbrains.zeppelin.service.SbtService


class IdeaTest extends AbstractScalaTest {
  test("Zeppelin.RunPackage") {
    val jar = SbtService.packageToJarCurrentProject("../..")
    assert(jar.endsWith(".jar"))
  }
}
