package samples

import org.junit._
import Assert._

@Test
class AppTest {

    @Test
    def testOK() = assertTrue(true)

//    @Test
//    def testKO() = assertTrue(false)

    @Test
    def testJavaObj() = assertEquals(new JavaObj().doStuff(), 33)

    @Test
    def testUseJavaObj() = assertEquals(new UseJavaObj().use(), 66)

    @Test
    def testScalaObj() = assertEquals(new ScalaObj().foo(), 280)

    @Test
    def testUseScalaObj() = assertEquals(new UseScalaObj().use(), -280)
}


