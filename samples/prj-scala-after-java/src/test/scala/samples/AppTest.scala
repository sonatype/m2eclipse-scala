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
    def testUseJavaObj() = assertEquals(new UseJavaObj().use(), 33)
}


