package org.beifengtz.jvmm.aop.wrapper;

import org.beifengtz.jvmm.aop.agent.AcrossThreadAgent;

import java.util.function.Function;

/**
 * description: 包装过的支持跨进程 Trace 的 Function，用法：
 * <pre>
 *     function(new WrappedSupplier(t -> {
 *         return t * 2;
 *     }));
 * </pre>
 * date: 10:14 2023/10/13
 *
 * @author beifengtz
 */
public class FunctionWrapper<T, R> implements Function<T, R> {
    private final Function<T, R> function;
    private final String contextId;
    private final long parentThreadId;

    public FunctionWrapper(Function<T, R> function) {
        if (function == null) {
            throw new NullPointerException("Function can not be null");
        }
        this.function = function;
        this.contextId = AcrossThreadAgent.getContextId();
        this.parentThreadId = Thread.currentThread().getId();
    }

    @Override
    public R apply(T t) {
        AcrossThreadAgent.setContextId(contextId);
        try {
            return function.apply(t);
        } finally {
            if (parentThreadId != Thread.currentThread().getId()) {
                AcrossThreadAgent.setContextId(null);
            }
        }
    }
}
