package org.beifengtz.jvmm.aop.wrapper;

import org.beifengtz.jvmm.aop.agent.AcrossThreadAgent;

import java.util.function.Supplier;

/**
 * description: 包装过的支持跨进程 Trace 的 Supplier，用法：
 * <pre>
 *     function(new WrappedSupplier(() -> {
 *         return 1;
 *     }));
 * </pre>
 * date: 10:10 2023/10/13
 *
 * @author beifengtz
 */
public class SupplierWrapper<V> implements Supplier<V> {

    private final Supplier<V> supplier;
    private final String contextId;
    private final long parentThreadId;

    public SupplierWrapper(Supplier<V> supplier) {
        if (supplier == null) {
            throw new NullPointerException("Supplier can not be null");
        }
        this.supplier = supplier;
        this.parentThreadId = Thread.currentThread().getId();
        this.contextId = AcrossThreadAgent.getContextId();
    }

    @Override
    public V get() {
        AcrossThreadAgent.setContextId(contextId);
        try {
            return supplier.get();
        } finally {
            if (parentThreadId != Thread.currentThread().getId()) {
                AcrossThreadAgent.setContextId(null);
            }
        }
    }
}
