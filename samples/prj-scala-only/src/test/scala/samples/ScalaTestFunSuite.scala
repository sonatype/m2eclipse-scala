package samples

import org.scalatest.BeforeAndAfterAll
import _root_.org.scalatest.junit.JUnitSuite
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.FunSuite
import org.scalatest.prop.Checkers

@RunWith(classOf[JUnitRunner])
class ScalaTestFunSuite extends FunSuite  with Checkers with BeforeAndAfterAll {
  import scala.collection.mutable.Stack
  // ...
  
  override def beforeAll(configMap: Map[String, Any]) {
	 //require(false, "false")
	 // fail("tt")
  }
  
  test("pop is invoked on a non-empty stack") {
 
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    val oldSize = stack.size
    val result = stack.pop()
    assert(result === 2)
    assert(stack.size === oldSize - 1 )
  }
 
  test("pop is invoked on an empty stack") (pending)
  
  test("concat list") {
    check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size )
  }
}
