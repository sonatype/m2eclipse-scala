package samples

import org.specs._

//import org.specs.runner.JUnit4
//class SpecsSampleTest extends JUnit4(SpecsSample)
//object SpecsSample extends Specification with ScalaCheck {

class SpecsSampleTest extends SpecificationWithJUnit  with ScalaCheck {

//import org.junit.runner.RunWith
//import org.specs.runner.{ JUnitSuiteRunner, JUnit }
//@RunWith(classOf[JUnitSuiteRunner])
//class SpecsSampleTest extends Specification with JUnit with ScalaCheck {

  "'hello world' has 11 characters" in {
     "hello world".size must be equalTo(11)
  }
  "'hello world' matches 'h.* w.*'" in {
     "hello world" must be matching("h.* w.*")
  }
  
  "startsWith" verifies { (a: String, b: String) => (a + b).startsWith(a) }
  "endsWith" verifies { (a: String, b: String) => (a + b).endsWith(b) }
  
  "These examples" should {
    skip("those examples don't pass yet")
    "be skipped" in { /*... */}
    "be skipped2" in { /*... */}
  }
  
  "my framework" should {
    "work with Xxxx" in {
      // skip the example if the DB2 connection is not available locally
      false must beTrue.orSkipExample // alias orSkip
      // use DB2Connection
    }
    /*...*/
  }
}