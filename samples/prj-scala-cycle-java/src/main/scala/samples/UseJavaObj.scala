package samples

/**
 * Hello world!
 *
 */
class UseJavaObj {
  def use() : Int = use(new JavaObj())
  def use(o : JavaObj) = o.doStuff() * 2
}
