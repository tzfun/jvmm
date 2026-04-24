package org.beifengtz.jvmm.aop.core.transformer;

import org.beifengtz.jvmm.aop.core.ExecutorEnhancer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * date: 10:30 2023/10/16
 *
 * @author beifengtz
 */
public class ExecutorTransformer implements ClassFileTransformer {

    private static final String THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ThreadPoolExecutor";
    private static final String SCHEDULED_THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ScheduledThreadPoolExecutor";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        className = className.replace("/", ".");
        if (THREAD_POOL_EXECUTOR_CLASS_NAME.equals(className) || SCHEDULED_THREAD_POOL_EXECUTOR_CLASS_NAME.equals(className)) {
            try {
                return ExecutorEnhancer.enhance(className, classFileBuffer);
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        }
        return null;
    }
}
