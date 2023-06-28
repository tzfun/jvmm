package org.beifengtz.jvmm.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: TODO
 * date: 11:36 2023/6/28
 *
 * @author beifengtz
 */
public class MethodWeaver extends ClassVisitor implements Opcodes {

    private static final Map<Integer, MethodListener> LISTENER_MAP = new ConcurrentHashMap<>();
    private static final Type WEAVER_TYPE = Type.getType(MethodWeaver.class);
    private static final Type CLASS_TYPE = Type.getType(Class.class);
    private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
    private static final Type OBJECT_TYPE = Type.getType(Object.class);
    /**
     * asm invoke {@link MethodWeaver#methodOnBefore(int, ClassLoader, String, String, String, Object, Object[])}
     */
    private static final Method ASM_METHOD_WEAVER_ON_BEFORE = getAsmMethod(
            MethodWeaver.class,
            "methodOnBefore",
            int.class,
            ClassLoader.class,
            String.class,
            String.class,
            String.class,
            Object.class,
            Object[].class);
    /**
     * asm invoke {@link MethodWeaver#methodOnReturning(Object)}
     */
    private static final Method ASM_METHOD_WEAVER_ON_RETURNING = getAsmMethod(
            MethodWeaver.class,
            "methodOnReturning",
            Object.class
    );
    /**
     * asm invoke {@link MethodWeaver#methodOnThrowing(Throwable)}
     */
    private static final Method ASM_METHOD_WEAVER_ON_THROWING = getAsmMethod(
            MethodWeaver.class,
            "methodOnThrowing",
            Throwable.class
    );
    private static final Method ASM_METHOD_CLASS_FOR_NAME = getAsmMethod(Class.class, "forName", String.class);
    private static final Method ASM_METHOD_OBJECT_GET_CLASS = getAsmMethod(Object.class, "getClass");
    private static final Method ASM_METHOD_CLASS_GET_CLASS_LOADER = getAsmMethod(Class.class, "getClassLoader");
    private static final ThreadLocal<Boolean> selfCalled = ThreadLocal.withInitial(() -> false);
    /**
     * 每一个被增强的方法，在处理完 before 之后就已经拿到了上下文，存入栈中，后续的切面函数可以从栈中获取
     */
    private static final ThreadLocal<Deque<Deque<Object>>> threadMethodContextStack = ThreadLocal.withInitial(LinkedList::new);

    public static Method getAsmMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        return Method.getMethod(getJavaMethod(clazz, methodName, parameterTypes));
    }

    private static java.lang.reflect.Method getJavaMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void methodOnBefore(int adviceId, ClassLoader classLoader, String className,
                                      String methodName, String methodDesc, Object target, Object[] args) {

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
            Deque<Object> methodContextStack = new LinkedList<>();

            methodContextStack.push(listener);
            methodContextStack.push(classLoader);
            methodContextStack.push(className);
            methodContextStack.push(methodName);
            methodContextStack.push(methodDesc);
            methodContextStack.push(target);
            methodContextStack.push(args);

            threadMethodContextStack.get().push(methodContextStack);

            doBefore(listener, classLoader, className, methodName, methodDesc, target, args);
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
            Deque<Object> methodContextStack = threadMethodContextStack.get().pop();

            Object[] args = (Object[]) methodContextStack.pop();
            Object target = methodContextStack.pop();
            String methodDesc = (String) methodContextStack.pop();
            String methodName = (String) methodContextStack.pop();
            String className = (String) methodContextStack.pop();
            ClassLoader classLoader = (ClassLoader) methodContextStack.pop();
            MethodListener adviceListener = (MethodListener) methodContextStack.pop();

            if (isThrowing) {
                doAfterThrowing(adviceListener, classLoader, className, methodName, methodDesc, target, args,
                        (Throwable) returnTarget);
            } else {
                doAfterReturning(adviceListener, classLoader, className, methodName, methodDesc, target, args,
                        returnTarget);
            }

        } finally {
            selfCalled.set(false);
        }
    }

    private static void doBefore(MethodListener adviceListener, ClassLoader loader, String className,
                                 String methodName, String methodDesc, Object target, Object[] args) {
        if (adviceListener != null) {
            try {
                adviceListener.before(loader, className, methodName, methodDesc, target, args);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void doAfterReturning(MethodListener adviceListener, ClassLoader classLoader, String className,
                                         String methodName, String methodDesc, Object target,
                                         Object[] args, Object returnObj) {
        if (adviceListener != null) {
            try {
                adviceListener.after(classLoader, className, methodName, methodDesc, target, args, returnObj);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static void doAfterThrowing(MethodListener adviceListener, ClassLoader loader, String className,
                                        String methodName, String methodDesc, Object target,
                                        Object[] args, Throwable throwable) {
        if (adviceListener != null) {
            try {
                adviceListener.error(loader, className, methodName, methodDesc, target, args, throwable);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static boolean isAbstract(int access) {
        return (ACC_ABSTRACT & access) == ACC_ABSTRACT;
    }

    private static boolean isFinal(int methodAccess) {
        return (ACC_FINAL & methodAccess) == ACC_FINAL;
    }

    private static boolean isStatic(int methodAccess) {
        return (ACC_STATIC & methodAccess) == ACC_STATIC;
    }

    private final String className;
    private final int adviceId;

    public MethodWeaver(int adviceId, MethodListener adviceListener, Class<?> targetClass, ClassVisitor cv) {
        super(ASM5, cv);
        this.adviceId = adviceId;
        this.className = targetClass.getName();
        LISTENER_MAP.put(adviceId, adviceListener);
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

        return new AdviceAdapter(ASM5, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc) {

            final Label beginLabel = new Label();

            final Label endLabel = new Label();

            /**
             * invoke {@link MethodWeaver#methodOnBefore} by asm
             */
            @Override
            protected void onMethodEnter() {
                //  fill arg: adviceId
                push(adviceId);
                box(Type.getType(Integer.class));

                //  fill arg: classLoader
                loadClassLoader();
                //  fill arg: className
                push(className);
                //  fill arg: methodName
                push(name);
                //  fill arg: methodDesc
                push(desc);
                //  fill arg: target
                loadThisOrNullIfStatic();
                //  fill arg: method args
                loadArgArray();

                //  invoke
                invokeStatic(WEAVER_TYPE, ASM_METHOD_WEAVER_ON_BEFORE);

                //  标记 try-catch-finally 代码段起始位置
                mark(beginLabel);
            }

            /**
             * invoke {@link MethodWeaver#methodOnReturning(Object)} by asm
             *
             * @param opcode one of {@link Opcodes#RETURN}, {@link Opcodes#IRETURN}, {@link Opcodes#FRETURN},
             *     {@link Opcodes#ARETURN}, {@link Opcodes#LRETURN}, {@link Opcodes#DRETURN} or {@link
             *     Opcodes#ATHROW}.
             */
            @Override
            protected void onMethodExit(int opcode) {
                if (ATHROW != opcode) {
                    //  根据返回码获取返回值
                    //  fill arg: return value
                    loadReturn(opcode);
                    invokeStatic(WEAVER_TYPE, ASM_METHOD_WEAVER_ON_RETURNING);
                }
            }

            /**
             * invoke {@link MethodWeaver#methodOnThrowing(Throwable)} by asm
             *
             * @param maxStack maximum stack size of the method.
             * @param maxLocals maximum number of local variables for the method.
             */
            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                //  在visitEnd之前调用，构造try-catch代码块
                mark(endLabel);
                //  beginLabel 与 endLabel 之间使用try-catch包住
                catchException(beginLabel, endLabel, THROWABLE_TYPE);
                //  从栈顶加载异常
                //  fill arg: throwable
                dup();
                invokeStatic(WEAVER_TYPE, ASM_METHOD_WEAVER_ON_THROWING);
                //  抛出原有异常
                throwException();

                super.visitMaxs(maxStack, maxLocals);
            }

            private void loadThisOrNullIfStatic() {
                if (MethodWeaver.isStatic(methodAccess)) {
                    push((Type) null);
                } else {
                    loadThis();
                }
            }

            private void loadReturn(int opcode) {
                switch (opcode) {
                    case RETURN: {
                        push((Type) null);
                        break;
                    }
                    case ARETURN: {
                        dup();
                        break;
                    }
                    case LRETURN:
                    case DRETURN: {
                        dup2();
                        box(Type.getReturnType(methodDesc));
                        break;
                    }
                    default: {
                        dup();
                        box(Type.getReturnType(methodDesc));
                        break;
                    }
                }
            }

            private void loadClassLoader() {
                if (MethodWeaver.isStatic(methodAccess)) {
                    //  Class.forName(clazz);
                    visitLdcInsn(className.replace("/", "."));
                    invokeStatic(CLASS_TYPE, ASM_METHOD_CLASS_FOR_NAME);
                } else {
                    //  this.getClass();
                    loadThis();
                    invokeVirtual(OBJECT_TYPE, ASM_METHOD_OBJECT_GET_CLASS);
                }
                //  getClassLoader();
                invokeVirtual(CLASS_TYPE, ASM_METHOD_CLASS_GET_CLASS_LOADER);
            }
        };
    }

    private boolean isIgnore(MethodVisitor mv, int access, String methodName) {
        return null == mv
                || isAbstract(access)
                || isFinal(access)
                || "<clinit>".equals(methodName)
                || "<init>".equals(methodName);
    }
}
