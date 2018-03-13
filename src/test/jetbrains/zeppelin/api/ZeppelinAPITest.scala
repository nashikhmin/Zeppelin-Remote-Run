package jetbrains.zeppelin.api

class ZeppelinAPITest extends org.scalatest.FunSuite {
  test("RestAPI.createNotebookAndParagraph") {
    val restAPI = new RestAPI("localhost", 8080)
    val zeppelinAPI = new ZeppelinApi(restAPI)
    val notebook = zeppelinAPI.createNotebook("a")
    assert(notebook.id.length == 9)
    val paragraph = zeppelinAPI.createParagraph(notebook.id)
    assert(paragraph.paragraphId.nonEmpty)
  }
}
