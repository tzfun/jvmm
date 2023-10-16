package org.beifengtz.jvmm.aop.wrapper;

import org.beifengtz.jvmm.aop.core.Attributes;
import org.beifengtz.jvmm.aop.core.ThreadLocalStore;

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
    private final Attributes attributes;
    private final long parentThreadId;

    protected RunnableWrapper(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException("Runnable can not be null");
        }
        this.runnable = runnable;
        this.parentThreadId = Thread.currentThread().getId();
        this.attributes = ThreadLocalStore.cloneAttributes();
    }

    @Override
    public void run() {
        ThreadLocalStore.setAttributes(attributes);
        try {
            runnable.run();
        } finally {
            if (parentThreadId != Thread.currentThread().getId()) {
                ThreadLocalStore.setAttributes(null);
            }
        }
    }
}
