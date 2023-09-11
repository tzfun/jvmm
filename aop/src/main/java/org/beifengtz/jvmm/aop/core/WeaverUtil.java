package org.beifengtz.jvmm.aop.core;

import org.beifengtz.jvmm.aop.agent.RunnableAgent;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

/**
 * description TODO
 * date 16:38 2023/9/11
 *
 * @author beifengtz
 */
public class WeaverUtil {
    static final Type ENHANCER_TYPE = Type.getType(MethodEnhancer.class);
    static final Type CLASS_TYPE = Type.getType(Class.class);
    static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
    static final Type OBJECT_TYPE = Type.getType(Object.class);
    static final Method ASM_METHOD_CLASS_FOR_NAME = getAsmMethod(Class.class, "forName", String.class);
    static final Method ASM_METHOD_OBJECT_GET_CLASS = getAsmMethod(Object.class, "getClass");
    static final Method ASM_METHOD_CLASS_GET_CLASS_LOADER = getAsmMethod(Class.class, "getClassLoader");
    static final Type EXECUTOR_ENHANCER_TYPE = Type.getType(ExecutorEnhancer.class);
    static final Type RUNNABLE_AGENT_TYPE = Type.getType(RunnableAgent.class);

    /**
     * asm invoke {@link MethodEnhancer#methodOnBefore(int, ClassLoader, String, String, String, Object, Object[])}
     */
    static final Method ASM_METHOD_WEAVER_ON_BEFORE = getAsmMethod(
            MethodEnhancer.class,
            "methodOnBefore",
            int.class,
            ClassLoader.class,
            String.class,
            String.class,
            String.class,
            Object.class,
            Object[].class);
    /**
     * asm invoke {@link MethodEnhancer#methodOnReturning(Object)}
     */
    static final Method ASM_METHOD_WEAVER_ON_RETURNING = getAsmMethod(
            MethodEnhancer.class,
            "methodOnReturning",
            Object.class
    );
    /**
     * asm invoke {@link MethodEnhancer#methodOnThrowing(Throwable)}
     */
    static final Method ASM_METHOD_WEAVER_ON_THROWING = getAsmMethod(
            MethodEnhancer.class,
            "methodOnThrowing",
            Throwable.class
    );
    /**
     * asm invoke {@link MethodEnhancer#methodOnThrowing(Throwable)}
     */
    static final Method ASM_EXECUTOR_ENHANCER_GET_CONTEXT_ID = getAsmMethod(
            ExecutorEnhancer.class,
            "getContextId"
    );


    public static Method getAsmMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        return Method.getMethod(getJavaMethod(clazz, methodName, parameterTypes));
    }

    public static java.lang.reflect.Method getJavaMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
