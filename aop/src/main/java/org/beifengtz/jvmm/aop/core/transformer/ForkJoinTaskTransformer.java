package org.beifengtz.jvmm.aop.core.transformer;

import org.beifengtz.jvmm.aop.core.ForkJoinTaskEnhancer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * date: 20:19 2023/10/16
 *
 * @author beifengtz
 */
public class ForkJoinTaskTransformer implements ClassFileTransformer {
    private static final String FORK_JOIN_TASK_CLASS_NAME = "java.util.concurrent.ForkJoinTask";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        className = className.replace("/", ".");
        if (className.equals(FORK_JOIN_TASK_CLASS_NAME)) {
            try {
                return ForkJoinTaskEnhancer.enhance(classfileBuffer);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
