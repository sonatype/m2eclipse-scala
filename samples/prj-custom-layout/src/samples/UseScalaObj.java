package samples;

public class UseScalaObj {

	public String use() throws Exception {
		return use(new ScalaObj());
	}

	public String use(ScalaObj o) throws Exception {
		return "java + " +  o.foo();
	}
}
