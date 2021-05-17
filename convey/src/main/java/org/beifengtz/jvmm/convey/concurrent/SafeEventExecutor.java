package org.beifengtz.jvmm.convey.concurrent;

import io.netty.util.concurrent.SingleThreadEventExecutor;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 16:12 2021/5/17
 *
 * @author beifengtz
 */
final class SafeEventExecutor extends SingleThreadEventExecutor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SafeEventExecutor.class);

    SafeEventExecutor(SafeEventExecutorGroup parent, Executor executor) {
        super(parent, executor, true);
    }

    @Override
    protected void run() {
        for (; ; ) {
            try {
                Runnable task = takeTask();
                if (task != null) {
                    task.run();
                    updateLastExecutionTime();
                }

                if (confirmShutdown()) {
                    break;
                }
            } catch (Throwable e) {
                logger.error(e.toString(), e);
            }
        }
    }
}