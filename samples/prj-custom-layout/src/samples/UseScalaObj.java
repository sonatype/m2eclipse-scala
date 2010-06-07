package samples;

public class UseScalaObj {

	public int use() throws Exception {
		return use(new ScalaObj());
	}

	public int use(ScalaObj o) throws Exception {
		return -1 * o.foo();
	}
}
