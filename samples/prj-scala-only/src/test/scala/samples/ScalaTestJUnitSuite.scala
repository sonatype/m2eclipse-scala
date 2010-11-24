package samples

import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import scala.collection.mutable.ListBuffer
import org.junit.Test
import org.junit.Before
import org.scalatest.prop.Checkers

// following import generate NPE into eclipse
//import org.scalacheck.Arbitrary._ 
//import org.scalacheck.Prop._

class ScalaTestJUnitSuite extends JUnitSuite with ShouldMatchersForJUnit with Checkers {

  var sb: StringBuilder = _
  var lb: ListBuffer[String] = _

  @Before
  def initialize() {
    sb = new StringBuilder("ScalaTest is ")
    lb = new ListBuffer[String]
  }

  @Test
  def verifyEasy() { // Uses ScalaTest assertions
    sb.append("easy!")
    assert(sb.toString === "ScalaTest is easy!")
    assert(lb.isEmpty)
    lb += "sweet"
    intercept[StringIndexOutOfBoundsException] {
      "concise".charAt(-1)
    }
  }

  @Test
  def pending_test() { // Uses ScalaTest assertions
	  pending
  }
  
  @Test
  def verifyFun() { // Uses ScalaTest matchers
    sb.append("fun!")
    sb.toString should be("ScalaTest is fun!")
    lb should be('empty)
    lb += "sweet"
    evaluating { "concise".charAt(-1) } should produce[StringIndexOutOfBoundsException]
  }

  @Test
  def scalacheck_call() {
    check((a: List[Int], b: List[Int]) => a.size + b.size == (a ::: b).size)
  }
}