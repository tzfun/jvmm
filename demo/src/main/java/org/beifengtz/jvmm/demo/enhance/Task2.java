package org.beifengtz.jvmm.demo.enhance;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * description: TODO
 * date: 15:22 2023/10/12
 *
 * @author beifengtz
 */
public class Task2 implements Runnable {

    private final Executor executor;
    private final CountDownLatch cdl;
    public Task2(Executor executor, CountDownLatch cdl) {
        this.executor = executor;
        this.cdl= cdl;
    }

    @Override
    public void run() {
        EnhanceDemo.calculate(new Random(hashCode()).nextInt(20));
        executor.execute(new Task3(cdl));
    }
}
