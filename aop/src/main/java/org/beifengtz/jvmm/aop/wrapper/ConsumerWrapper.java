package org.beifengtz.jvmm.aop.wrapper;

import org.beifengtz.jvmm.aop.core.Attributes;
import org.beifengtz.jvmm.aop.core.ThreadLocalStore;

import java.util.function.Consumer;

/**
 * description: 包装过的支持跨进程 Trace 的 Consumer，用法：
 * <pre>
 *     function(new WrappedConsumer(value -> {}));
 * </pre>
 * date: 10:02 2023/10/13
 *
 * @author beifengtz
 */
public class ConsumerWrapper<T> implements Consumer<T> {

    private final Consumer<T> consumer;
    private final Attributes attributes;
    private final long parentThreadId;

    public ConsumerWrapper(Consumer<T> consumer) {
        if (consumer == null) {
            throw new NullPointerException("Consumer can not be null");
        }
        this.consumer = consumer;
        this.parentThreadId = Thread.currentThread().getId();
        this.attributes = ThreadLocalStore.cloneAttributes();
    }

    @Override
    public void accept(T t) {
        ThreadLocalStore.setAttributes(attributes);
        try {
            consumer.accept(t);
        } finally {
            if (parentThreadId != Thread.currentThread().getId()) {
                ThreadLocalStore.setAttributes(null);
            }
        }
    }
}
