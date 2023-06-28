package org.beifengtz.jvmm.asm.test;

import java.util.Random;

/**
 * description: TODO
 * date: 18:14 2023/6/28
 *
 * @author beifengtz
 */
public class People {
    int counter;

    public void say() throws Exception {
        System.out.println("Say number " + counter++);
        Thread.sleep(new Random().nextInt(3) * 1000);
    }
}
