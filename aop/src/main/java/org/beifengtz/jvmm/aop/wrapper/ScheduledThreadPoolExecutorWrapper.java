package org.beifengtz.jvmm.aop.wrapper;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * description: TODO
 * date: 14:30 2023/10/12
 *
 * @author beifengtz
 */
public class ScheduledThreadPoolExecutorWrapper extends ScheduledThreadPoolExecutor {
    public ScheduledThreadPoolExecutorWrapper(int corePoolSize) {
        super(corePoolSize);
    }

    public ScheduledThreadPoolExecutorWrapper(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public ScheduledThreadPoolExecutorWrapper(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public ScheduledThreadPoolExecutorWrapper(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        //  将调用线程的context id传递给执行线程
        super.execute(new RunnableWrapper(command));
    }
}
