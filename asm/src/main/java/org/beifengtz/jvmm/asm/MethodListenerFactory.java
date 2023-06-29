package org.beifengtz.jvmm.asm;

/**
 * description: TODO
 * date: 20:19 2023/6/28
 *
 * @author beifengtz
 */
public interface MethodListenerFactory {
    MethodListener create(String className, String methodName);
}
