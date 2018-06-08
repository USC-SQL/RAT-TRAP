package usc.sql.testcase;

public class Polymorpic {
	
	public void test1()
	{
		Animal dog = new Dog();
		dog.eat();
	}
}
class Animal {
	void eat() {
		System.currentTimeMillis();
	}
}

class Dog extends Animal {
	void eat() {
		int a = 1;
		System.out.println("Dog");
	}
}

class Wolf extends Animal {
	void eat() {
		System.out.println("Wolf");
	}
}