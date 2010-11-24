package samples

/**
 * Hello world!
 * @author dwayne
 */
object App {
  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b) //println(x)
  
  def main(args : Array[String]) {
    println( "Hello World!" )
    foo(args)
  }

}
