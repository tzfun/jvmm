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
     * @param classLoader 目标方法所在类的 {@link ClassLoader}
     * @param className   目标方法所在类的类名
     * @param methodName  目标方法名
     * @param methodDesc  目标方法描述
     * @param target      目标方法所在类实例对象，如果为静态方法，则为null
     * @param args        目标方法传入参数
     * @throws Throwable 触发error
     */
    void before(ClassLoader classLoader, String className, String methodName, String methodDesc, Object target,
                Object[] args) throws Throwable;

    /**
     * 方法正常返回后触发
     *
     * @param classLoader 目标方法所在类的 {@link ClassLoader}
     * @param className   目标方法所在类的类名
     * @param methodName  目标方法名
     * @param methodDesc  目标方法描述
     * @param target      目标方法所在类实例对象，如果为静态方法，则为null
     * @param args        目标方法传入参数
     * @param returnObj   目标方法返回值
     * @throws Throwable 通知执行过程中的异常
     */
    void after(ClassLoader classLoader, String className, String methodName, String methodDesc, Object target,
               Object[] args, Object returnObj) throws Throwable;

    /**
     * 方法抛出异常后触发
     *
     * @param classLoader 目标方法所在类的 {@link ClassLoader}
     * @param className   目标方法所在类的类名
     * @param methodName  目标方法名
     * @param methodDesc  目标方法描述
     * @param target      目标方法所在类实例对象，如果为静态方法，则为null
     * @param args        目标方法传入参数
     * @param throwable   目标方法执行时抛出的异常 {@link Throwable}
     * @throws Throwable 通知执行过程中的异常
     */
    void error(ClassLoader classLoader, String className, String methodName, String methodDesc, Object target,
               Object[] args, Throwable throwable) throws Throwable;
}
