package org.beifengtz.jvmm.aop.wrapper;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * description: TODO
 * date: 10:22 2023/10/16
 *
 * @author beifengtz
 */
public class Utils {
    public static Runnable wrap(Runnable runnable) {
        return new RunnableWrapper(runnable);
    }

    public static <V> Callable<V> wrap(Callable<V> callable) {
        return new CallableWrapper<>(callable);
    }

    public static <T> Consumer<T> wrap(Consumer<T> consumer) {
        return new ConsumerWrapper<>(consumer);
    }

    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        return new SupplierWrapper<>(supplier);
    }

    public static <T, R> Function<T, R> wrap(Function<T, R> function) {
        return new FunctionWrapper<>(function);
    }
}
