package org.beifengtz.jvmm.asm;

/**
 * description: {@link MethodListener}工厂接口
 * date: 20:19 2023/6/28
 *
 * @author beifengtz
 */
public interface MethodListenerFactory {
    /**
     * 获取{@link MethodListener}监听器
     *
     * @param className  类全限名
     * @param methodName 方法名
     * @return {@link MethodListener}对象
     */
    MethodListener getListener(String className, String methodName);
}
