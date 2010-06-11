package test;

public aspect TestAspect {
	void around() : execution(public void *..aMethod()) {
		System.out.print("TestAspect");
	}
}
