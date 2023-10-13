package org.beifengtz.jvmm.aop.wrapper;

import org.beifengtz.jvmm.aop.agent.AcrossThreadAgent;

/**
 * description: 包装过的支持跨进程 Trace 的 Runnable，用法：
 * <pre>
 *     executor.execute(new WrappedRunnable(() -> {}));
 * </pre>
 * date: 9:48 2023/10/13
 *
 * @author beifengtz
 */
public class RunnableWrapper implements Runnable {

    private final Runnable runnable;
    private final String contextId;
    private final long parentThreadId;

    public RunnableWrapper(Runnable runnable) {
        this(runnable, AcrossThreadAgent.getContextId());
        if (runnable == null) {
            throw new NullPointerException("Runnable can not be null");
        }
    }

    protected RunnableWrapper(Runnable runnable, String contextId) {
        this.runnable = runnable;
        this.parentThreadId = Thread.currentThread().getId();
        this.contextId = contextId;
    }

    @Override
    public void run() {
        AcrossThreadAgent.setContextId(contextId);
        try {
            runnable.run();
        } finally {
            if (parentThreadId != Thread.currentThread().getId()) {
                AcrossThreadAgent.setContextId(null);
            }
        }
    }
}
