package org.beifengtz.jvmm.asm;

/**
 * description: TODO
 * date: 11:29 2023/6/28
 *
 * @author beifengtz
 */
public interface MethodListener {
    /**
     * 方法执行前触发
     *
     * @param info 目标方法信息{@link MethodInfo}
     * @throws Throwable 触发error
     */
    void before(MethodInfo info) throws Throwable;

    /**
     * 方法正常返回后触发
     *
     * @param info        目标方法信息{@link MethodInfo}
     * @param returnValue 目标方法返回值
     * @throws Throwable 通知执行过程中的异常
     */
    void after(MethodInfo info, Object returnValue) throws Throwable;

    /**
     * 方法抛出异常后触发
     *
     * @param info      目标方法信息{@link MethodInfo}
     * @param throwable 目标方法执行时抛出的异常 {@link Throwable}
     * @throws Throwable 通知执行过程中的异常
     */
    void error(MethodInfo info, Throwable throwable) throws Throwable;
}
