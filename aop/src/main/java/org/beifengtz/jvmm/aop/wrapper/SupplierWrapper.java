package org.beifengtz.jvmm.aop.wrapper;

import org.beifengtz.jvmm.aop.core.Attributes;
import org.beifengtz.jvmm.aop.core.ThreadLocalStore;

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
    private final Attributes attributes;

    public SupplierWrapper(Supplier<V> supplier) {
        if (supplier == null) {
            throw new NullPointerException("Supplier can not be null");
        }
        this.supplier = supplier;
        this.attributes = ThreadLocalStore.cloneAttributes();
    }

    @Override
    public V get() {
        Attributes previous = ThreadLocalStore.getAttributes();
        ThreadLocalStore.setAttributes(attributes);
        try {
            return supplier.get();
        } finally {
            ThreadLocalStore.setAttributes(previous);
        }
    }
}
