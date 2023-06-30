package org.beifengtz.jvmm.aop.core;

/**
 * description: 方法监听器，当执行被增强方法时，会触发相应时点的方法
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
    void afterReturning(MethodInfo info, Object returnValue) throws Throwable;

    /**
     * 方法抛出异常后触发
     *
     * @param info      目标方法信息{@link MethodInfo}
     * @param throwable 目标方法执行时抛出的异常 {@link Throwable}
     * @throws Throwable 通知执行过程中的异常
     */
    void afterThrowing(MethodInfo info, Throwable throwable) throws Throwable;

    /**
     * 方法结束时执行，无论正常返回还是抛出异常，都会触发此方法
     *
     * @param info      目标方法信息{@link MethodInfo}
     * @param returnVal 如果正常返回，此值为返回值；如果抛出异常，此值为 null
     * @param throwable 如果正常返回，此参数为 null；如果抛出异常，此值为异常对象
     * @throws Throwable 通知执行过程中的异常
     */
    void after(MethodInfo info, Object returnVal, Throwable throwable) throws Throwable;
}
