package di;

public class DependencyI {
	public void aMethod() {
		System.out.println("DependencyI");
	}
	
	public static void main(String[] args) {
		new DependencyI().aMethod();
	}
}
