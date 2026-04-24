package org.beifengtz.jvmm.aop.core;

import org.beifengtz.jvmm.aop.listener.MethodListener;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * date: 11:36 2023/6/28
 *
 * @author beifengtz
 */
public class MethodEnhancer extends ClassVisitor implements Opcodes {

    private static final AtomicInteger LISTENER_ID_GENERATOR = new AtomicInteger(0);
    private static final Map<Integer, MethodListener> LISTENER_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, MethodListener> ENHANCED_METHOD = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> selfCalled = ThreadLocal.withInitial(() -> false);
    /**
     * 每一个被增强的方法，在处理完 before 之后就已经拿到了上下文，存入栈中，后续的切面函数可以从栈中获取
     */
    private static final ThreadLocal<Deque<MethodAttach>> threadMethodAttachStack = ThreadLocal.withInitial(LinkedList::new);

    /**
     * 重置所有增强状态，清除所有已注册的 listener 和增强记录。
     * 通常在卸载 agent 或需要重新增强时调用。
     * <p>
     * 注意：调用此方法后，已经注入到字节码中的 adviceId 将找不到对应的 listener，
     * 对应的增强方法会打印警告并跳过切面逻辑，不会影响业务方法的正常执行。
     * 如果需要彻底清除增强效果，应配合 {@link java.lang.instrument.Instrumentation#retransformClasses} 还原字节码。
     */
    public static void reset() {
        LISTENER_MAP.clear();
        ENHANCED_METHOD.clear();
        LISTENER_ID_GENERATOR.set(0);
    }

    /**
     * 移除指定的 listener，同时清理其关联的增强记录。
     *
     * @param listener 要移除的 {@link MethodListener}
     * @return 被移除的 adviceId 列表，可用于日志追踪
     */
    public static List<Integer> removeListener(MethodListener listener) {
        if (listener == null) {
            return new ArrayList<>();
        }

        //  清理 LISTENER_MAP 中该 listener 对应的所有 adviceId
        List<Integer> removedIds = new ArrayList<>();
        Iterator<Map.Entry<Integer, MethodListener>> it = LISTENER_MAP.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, MethodListener> entry = it.next();
            if (entry.getValue() == listener) {
                removedIds.add(entry.getKey());
                it.remove();
            }
        }

        //  清理 ENHANCED_METHOD 中该 listener 对应的记录
        ENHANCED_METHOD.entrySet().removeIf(entry -> entry.getValue() == listener);

        return removedIds;
    }

    public static void methodOnBefore(int adviceId, ClassLoader classLoader, String className, String methodName,
                                      String methodDesc, Object target, Object[] args) {

        if (selfCalled.get()) {
            return;
        } else {
            selfCalled.set(true);
        }
        try {
            MethodListener listener = LISTENER_MAP.get(adviceId);
            if (listener == null) {
                System.err.println("[jvmm] WARN: no listener for adviceId=" + adviceId);
                return;
            }

            //  方法调用开始，保存上下文入栈，后续 methodOnEnd 可以从此获取，而不必再传这些参数
            MethodAttach methodAttach = new MethodAttach().setListener(listener);
            methodAttach.setClassLoader(classLoader)
                    .setClassName(className)
                    .setMethodName(methodName)
                    .setMethodDesc(methodDesc)
                    .setTarget(target)
                    .setArgs(args);

            threadMethodAttachStack.get().push(methodAttach);

            doBefore(listener, methodAttach);
        } finally {
            selfCalled.set(false);
        }
    }

    public static void methodOnReturning(Object returnVal) {
        methodOnEnd(false, returnVal);
    }

    public static void methodOnThrowing(Throwable throwable) {
        methodOnEnd(true, throwable);
    }

    public static void methodOnEnd(boolean isThrowing, Object returnTarget) {
        if (selfCalled.get()) {
            return;
        } else {
            selfCalled.set(true);
        }

        //  从栈中恢复执行现场
        try {
            Deque<MethodAttach> stack = threadMethodAttachStack.get();
            if (stack.isEmpty()) {
                return;
            }
            MethodAttach methodAttach = stack.pop();

            MethodListener adviceListener = methodAttach.getListener();

            methodAttach.setListener(null);
            if (isThrowing) {
                doAfterThrowing(adviceListener, methodAttach, (Throwable) returnTarget);
            } else {
                doAfterReturning(adviceListener, methodAttach, returnTarget);
            }
            doAfter(adviceListener, methodAttach, isThrowing ? null : returnTarget, isThrowing ? (Throwable) returnTarget : null);
        } finally {
            selfCalled.set(false);
        }
    }

    private static void doBefore(MethodListener adviceListener, MethodInfo info) {
        if (adviceListener != null) {
            try {
                adviceListener.before(info);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }

    private static void doAfterReturning(MethodListener adviceListener, MethodInfo info, Object returnVal) {
        if (adviceListener != null) {
            try {
                adviceListener.afterReturning(info, returnVal);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }

    private static void doAfterThrowing(MethodListener adviceListener, MethodInfo info, Throwable throwable) {
        if (adviceListener != null) {
            try {
                adviceListener.afterThrowing(info, throwable);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }

    private static void doAfter(MethodListener adviceListener, MethodAttach info, Object returnVal, Throwable throwable) {
        if (adviceListener != null) {
            try {
                adviceListener.after(info, returnVal, throwable);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
    }


    private final String className;
    private final String methodRegex;
    private final String methodIgnoreRegex;
    private final MethodListener methodListener;

    public MethodEnhancer(MethodListener methodListener, String className, ClassVisitor cv) {
        this(methodListener, className, cv, ".*", null);
    }

    /**
     * {@link MethodEnhancer}构造函数
     *
     * @param methodListener    方法切面监听器 {@link MethodListener}
     * @param className         监听的类全限名，例如：org.beifengtz.jvmm.asm.Enhancer
     * @param cv                ASM {@link ClassVisitor}
     * @param methodRegex       正则表达式，针对于该类下满足匹配的方法进行增强。如果为 null 则默认为 .* 全匹配
     * @param methodIgnoreRegex 正则表达式，针对于该类下满足匹配的方法忽略，无需增强。如果为 null 则默认不忽略
     */
    public MethodEnhancer(MethodListener methodListener, String className, ClassVisitor cv, String methodRegex, String methodIgnoreRegex) {
        super(ASM9, cv);
        this.methodListener = methodListener;
        this.className = className;
        this.methodRegex = methodRegex == null ? ".*" : methodRegex;
        this.methodIgnoreRegex = methodIgnoreRegex;
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (isIgnore(mv, access, name)) {
            return mv;
        }

        if (methodListener == null) {
            return mv;
        }
        String methodKey = className + "#" + name + desc;
        MethodListener enhancedListener = ENHANCED_METHOD.get(methodKey);
        //  已经增强过，对重复增强的监听器进行合并
        if (enhancedListener != null) {
            enhancedListener.addListener(methodListener);
            return mv;
        }

        int adviceId = LISTENER_ID_GENERATOR.getAndIncrement();
        LISTENER_MAP.put(adviceId, methodListener);
        ENHANCED_METHOD.put(methodKey, methodListener);
        return new MethodWeaver(adviceId, className, mv, access, name, desc, signature, exceptions);
    }

    private boolean isIgnore(MethodVisitor mv, int access, String methodName) {
        return mv == null
                || WeaverUtil.isAbstract(access)
                || WeaverUtil.isFinal(access)
                || WeaverUtil.isNative(access)
                || WeaverUtil.isBridge(access)
                || WeaverUtil.isSynthetic(access)
                || "<clinit>".equals(methodName)
                || "<init>".equals(methodName)
                || (methodIgnoreRegex != null && methodName.matches(methodIgnoreRegex))
                || !methodName.matches(methodRegex);
    }
}
