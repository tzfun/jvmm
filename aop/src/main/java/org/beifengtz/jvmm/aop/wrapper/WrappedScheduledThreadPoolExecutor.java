package org.beifengtz.jvmm.aop.wrapper;

import org.beifengtz.jvmm.aop.agent.RunnableAgent;
import org.beifengtz.jvmm.aop.core.ExecutorEnhancer;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * description: TODO
 * date: 14:30 2023/10/12
 *
 * @author beifengtz
 */
public class WrappedScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    public WrappedScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public WrappedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public WrappedScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public WrappedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        //  将调用线程的context id传递给执行线程
        super.execute(new RunnableAgent(command, ExecutorEnhancer.getContextId()));
    }
}
