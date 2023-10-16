package org.beifengtz.jvmm.aop.core.transformer;

import org.beifengtz.jvmm.aop.core.ExecutorEnhancer;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * description: TODO
 * date: 10:30 2023/10/16
 *
 * @author beifengtz
 */
public class ExecutorTransformer implements ClassFileTransformer {

    private static final String THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ThreadPoolExecutor";
    private static final String SCHEDULED_THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ScheduledThreadPoolExecutor";

    private static final String FORK_JOIN_POOL_CLASS_NAME = "java.util.concurrent.ForkJoinPool";
    private static final String FORK_JOIN_WORKER_THREAD_FACTORY_CLASS_NAME_NAME = "java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replaceAll("/", ".");
        if (THREAD_POOL_EXECUTOR_CLASS_NAME.equals(className) || SCHEDULED_THREAD_POOL_EXECUTOR_CLASS_NAME.equals(className)) {
            try {
                System.out.println("Jvmm enhance executor: " + className);
                return ExecutorEnhancer.enhance(className, classfileBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
