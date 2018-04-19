package jetbrains.zeppelin.api

import jetbrains.zeppelin.service.SbtService
import org.scalatest.{FunSuite, Matchers}


class IdeaTest extends FunSuite with Matchers {
  test("Zeppelin.RunPackage") {
    val jar = SbtService.packageToJarCurrentProject("../..")
    assert(jar.endsWith(".jar"))
  }


}
