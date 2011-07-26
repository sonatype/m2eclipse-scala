package samples

import org.specs2._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class SpecsSampleTest extends mutable.Specification {

  "'hello world' has 11 characters" in {
     "hello world".size must be equalTo(11)
  }
  "'hello world' matches 'h.* w.*'" in {
     "hello world" must be matching("h.* w.*")
  }
}