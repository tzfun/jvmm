package org.beifengtz.jvmm.aop.core.transformer;

import org.beifengtz.jvmm.aop.core.ForkJoinTaskEnhancer;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.security.ProtectionDomain;

/**
 * description: TODO
 * date: 20:19 2023/10/16
 *
 * @author beifengtz
 */
public class ForkJoinTaskTransformer implements ClassFileTransformer {
    private static final String FORK_JOIN_TASK_CLASS_NAME = "java.util.concurrent.ForkJoinTask";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        className = className.replaceAll("/", ".");
        if (className.equals(FORK_JOIN_TASK_CLASS_NAME)) {
            try {
                System.out.println("Jvmm enhance: " + className);
                byte[] bytes = ForkJoinTaskEnhancer.enhance(classfileBuffer);
                Files.write(new File(className + ".class").toPath(), bytes);
                return bytes;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }
}
