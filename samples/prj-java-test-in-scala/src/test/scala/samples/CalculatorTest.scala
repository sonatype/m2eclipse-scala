package samples

class CalculatorTest {
  import org.junit._
  import Assert._

  @Test
  def testOK() = assertTrue(true)

  @Test
  def basicSum() = {
    val subject = new Calculator()
    assertEquals(10, subject.sum(5, 5))
    assertEquals(10, subject.sum(1, 9))
  }
}