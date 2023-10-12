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
public class Task1 implements Runnable {
    private final Executor executor;
    private final CountDownLatch cdl;

    public Task1(Executor executor, CountDownLatch cdl) {
        this.executor = executor;
        this.cdl = cdl;
    }

    @Override
    public void run() {
        EnhanceDemo.calculate(new Random(System.currentTimeMillis()).nextInt(20));
        executor.execute(new Task2(executor,cdl));
    }
}
