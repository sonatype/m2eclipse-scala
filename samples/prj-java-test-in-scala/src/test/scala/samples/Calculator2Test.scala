package samples

import org.junit.Assert._
import junit.framework.TestCase

class Calculator2Test extends TestCase {

  def test_ok() = assertTrue(true)

  def test_basicSum() = {
    val subject = new Calculator()
    assertEquals(10, subject.sum(5, 5))
    assertEquals(10, subject.sum(1, 9))
  }
}