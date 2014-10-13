package samples

import _root_.org.scalatest.junit.JUnitSuite
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.FunSuite


@RunWith(classOf[JUnitRunner])
class ScalaTestFunSuite extends FunSuite {

  test("An empty Set should have size 0") {
    assert(Set.empty.size === 0)
  }

  test("Invoking head on an empty Set should produce NoSuchElementException") {
    intercept[NoSuchElementException] {
      Set.empty.head
    }
  }
}
