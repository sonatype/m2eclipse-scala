package da;

public class DependencyA {
	public void aMethod() {
		System.out.println("DependencyA");
	}

	public static void main(String[] args) {
		new DependencyA().aMethod();
	}
}
