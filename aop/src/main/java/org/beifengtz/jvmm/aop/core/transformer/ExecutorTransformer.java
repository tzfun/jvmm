package org.beifengtz.jvmm.aop.core.transformer;

import org.beifengtz.jvmm.aop.core.ExecutorEnhancer;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
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

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        className = className.replaceAll("/", ".");
        if (THREAD_POOL_EXECUTOR_CLASS_NAME.equals(className) || SCHEDULED_THREAD_POOL_EXECUTOR_CLASS_NAME.equals(className)) {
            try {
                System.out.println("Jvmm enhance executor: " + className);
                byte[] bytes =  ExecutorEnhancer.enhance(className, classfileBuffer);
                Files.write(new File(className + ".class").toPath(), bytes);
                return bytes;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
