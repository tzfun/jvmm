package org.beifengtz.jvmm.aop.core;

import org.beifengtz.jvmm.aop.wrapper.Utils;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_FINAL;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_NATIVE;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_STATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * description TODO
 * date 16:38 2023/9/11
 *
 * @author beifengtz
 */
class WeaverUtil {
    public static final String JVMM_ATTRIBUTE_FIELD_NAME = "jvmm_attributes$$$";

    static final Type ENHANCER_TYPE = Type.getType(MethodEnhancer.class);
    static final Type CLASS_TYPE = Type.getType(Class.class);
    static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
    static final Type OBJECT_TYPE = Type.getType(Object.class);
    static final Method ASM_METHOD_CLASS_FOR_NAME = getAsmMethod(Class.class, "forName", String.class);
    static final Method ASM_METHOD_OBJECT_GET_CLASS = getAsmMethod(Object.class, "getClass");
    static final Method ASM_METHOD_CLASS_GET_CLASS_LOADER = getAsmMethod(Class.class, "getClassLoader");
    static final Type WRAPPER_UTILS = Type.getType(Utils.class);
    static final Type THREAD_LOCAL_STORE = Type.getType(ThreadLocalStore.class);
    static final Type FORK_JOIN_TASK = Type.getType("Ljava/util/concurrent/ForkJoinTask");

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
     * asm invoke {@link Utils#wrap(Runnable)}
     */
    static final Method ASM_UTILS_WRAP_RUNNABLE = getAsmMethod(
            Utils.class,
            "wrap",
            Runnable.class
    );
    /**
     * asm invoke {@link ThreadLocalStore#cloneAttributes()}
     */
    static final Method ASM_THREAD_LOCAL_STORE_CLONE_ATTRIBUTES = getAsmMethod(
            ThreadLocalStore.class,
            "cloneAttributes"
    );
    /**
     * asm invoke {@link ThreadLocalStore#setAttributes(Attributes)}
     */
    static final Method ASM_THREAD_LOCAL_STORE_SET_ATTRIBUTES = getAsmMethod(
            ThreadLocalStore.class,
            "setAttributes",
            Attributes.class
    );

    static Method getAsmMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        return Method.getMethod(getJavaMethod(clazz, methodName, parameterTypes));
    }

    static java.lang.reflect.Method getJavaMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean isAbstract(int access) {
        return (ACC_ABSTRACT & access) == ACC_ABSTRACT;
    }

    static boolean isFinal(int access) {
        return (ACC_FINAL & access) == ACC_FINAL;
    }

    static boolean isStatic(int access) {
        return (ACC_STATIC & access) == ACC_STATIC;
    }

    static boolean isInterface(int access) {
        return (ACC_INTERFACE & access) == ACC_INTERFACE;
    }

    static boolean isNative(int access) {
        return (ACC_NATIVE & access) == ACC_NATIVE;
    }

    static boolean isBridge(int access) {
        return (ACC_BRIDGE & access) == ACC_BRIDGE;
    }

    static boolean isSynthetic(int access) {
        return (ACC_SYNTHETIC & access) == ACC_SYNTHETIC;
    }
}
