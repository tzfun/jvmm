package org.beifengtz.jvmm.aop.wrapper;

import org.beifengtz.jvmm.aop.agent.AcrossThreadAgent;

import java.util.concurrent.Callable;

/**
 * description: 包装过的支持跨进程 Trace 的 Callable，用法：
 * <pre>
 *     executor.execute(new WrappedCallable(() -> 1));
 * </pre>
 * date: 9:57 2023/10/13
 *
 * @author beifengtz
 */
public class CallableWrapper<V> implements Callable<V> {

    private final Callable<V> callable;
    private final String contextId;
    private final long parentThreadId;

    public CallableWrapper(Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException("Callable can not be null");
        }
        this.callable = callable;
        this.parentThreadId = Thread.currentThread().getId();
        this.contextId = AcrossThreadAgent.getContextId();
    }

    @Override
    public V call() throws Exception {
        AcrossThreadAgent.setContextId(contextId);
        try {
            return callable.call();
        } finally {
            if (parentThreadId != Thread.currentThread().getId()) {
                AcrossThreadAgent.setContextId(null);
            }
        }
    }
}
