package org.beifengtz.jvmm.demo.enhance;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * description: TODO
 * date: 15:28 2023/10/12
 *
 * @author beifengtz
 */
public class Task3 implements Runnable {

    private final CountDownLatch cdl;
    public Task3(CountDownLatch cdl) {
        this.cdl = cdl;
    }

    @Override
    public void run() {
        EnhanceDemo.calculate(new Random(System.currentTimeMillis()).nextInt(20));
        cdl.countDown();
    }
}