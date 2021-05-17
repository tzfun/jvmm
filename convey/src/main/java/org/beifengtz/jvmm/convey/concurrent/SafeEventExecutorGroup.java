package org.beifengtz.jvmm.convey.concurrent;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:11 2021/5/17
 *
 * @author beifengtz
 */
public class SafeEventExecutorGroup extends MultithreadEventExecutorGroup {

    public SafeEventExecutorGroup(int nThreads) {
        this(nThreads, null);
    }

    public SafeEventExecutorGroup(int nThreads, ThreadFactory threadFactory) {
        super(nThreads, threadFactory);
    }

    @Override
    protected EventExecutor newChild(Executor executor, Object... args) throws Exception {
        return new SafeEventExecutor(this, executor);
    }
}
