package org.beifengtz.jvmm.asm.test;

/**
 * description: TODO
 * date: 18:14 2023/6/28
 *
 * @author beifengtz
 */
public class People {
    int counter;

    public void say() {
        System.out.println("Say " + counter++);
    }

    public void run() {
        System.out.println("Run " + counter++);
    }

    public void sayHello() {
        System.out.println("Say Hello " + counter++);
    }

    public void sayHelloWorld() {
        System.out.println("Say Hello World! " + counter++);
    }
}
