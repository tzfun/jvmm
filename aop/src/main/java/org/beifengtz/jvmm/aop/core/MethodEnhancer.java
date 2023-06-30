package org.beifengtz.jvmm.aop.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description: TODO
 * date: 11:36 2023/6/28
 *
 * @author beifengtz
 */
public class MethodEnhancer extends ClassVisitor implements Opcodes {

    private static final AtomicInteger LISTENER_ID_GENERATOR = new AtomicInteger(0);
    private static final Map<Integer, MethodListener> LISTENER_MAP = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> selfCalled = ThreadLocal.withInitial(() -> false);
    /**
     * 每一个被增强的方法，在处理完 before 之后就已经拿到了上下文，存入栈中，后续的切面函数可以从栈中获取
     */
    private static final ThreadLocal<Deque<MethodAttach>> threadMethodAttachStack = ThreadLocal.withInitial(LinkedList::new);

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
                throw new RuntimeException("no listener for:" + adviceId);
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
            MethodAttach methodAttach = threadMethodAttachStack.get().pop();

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
                t.printStackTrace();
            }
        }
    }

    private static void doAfterReturning(MethodListener adviceListener, MethodInfo info, Object returnVal) {
        if (adviceListener != null) {
            try {
                adviceListener.afterReturning(info, returnVal);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void doAfterThrowing(MethodListener adviceListener, MethodInfo info, Throwable throwable) {
        if (adviceListener != null) {
            try {
                adviceListener.afterThrowing(info, throwable);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void doAfter(MethodListener adviceListener, MethodAttach info, Object returnVal, Throwable throwable) {
        if (adviceListener != null) {
            try {
                adviceListener.after(info, returnVal, throwable);
            } catch (Throwable t) {
                t.printStackTrace();
            }
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
        super(ASM5, cv);
        System.out.println("==> enhance: " + className);
        this.methodListener = methodListener;
        this.className = className;
        this.methodRegex = methodRegex == null ? ".*" : methodRegex;
        this.methodIgnoreRegex = methodIgnoreRegex;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (isIgnore(mv, access, name)) {
            return mv;
        }
        MethodListener listener = null;
        if (methodListener != null) {
            listener = methodListener;
        }

        if (listener == null) {
            return mv;
        }

        int adviceId = LISTENER_ID_GENERATOR.getAndIncrement();
        LISTENER_MAP.put(adviceId, listener);
        return new MethodWeaver(adviceId, className, mv, access, name, desc, signature, exceptions);
    }

    private boolean isIgnore(MethodVisitor mv, int access, String methodName) {
        return null == mv
                || isInterface(access)
                || isAbstract(access)
                || isFinal(access)
                || "<clinit>".equals(methodName)
                || "<init>".equals(methodName)
                || (methodIgnoreRegex != null && methodName.matches(methodIgnoreRegex))
                || !methodName.matches(methodRegex);
    }
}
