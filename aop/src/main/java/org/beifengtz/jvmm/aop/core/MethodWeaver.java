package org.beifengtz.jvmm.aop.core;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import static org.beifengtz.jvmm.aop.core.WeaverUtil.*;

/**
 * description: TODO
 * date: 16:14 2023/6/28
 *
 * @author beifengtz
 */
public class MethodWeaver extends AdviceAdapter {

    private final int adviceId;
    private final String className;
    final Label beginLabel = new Label();

    final Label endLabel = new Label();

    /**
     * Constructs a new {@link AdviceAdapter}.
     *
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     */
    public MethodWeaver(int adviceId, String className, MethodVisitor mv, int access, String name,
                           String desc, String signature, String[] exceptions) {
        super(ASM9, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc);
        this.adviceId = adviceId;
        this.className = className;
    }

    /**
     * invoke {@link MethodEnhancer#methodOnBefore} by asm
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
        push(getName());
        //  fill arg: methodDesc
        push(methodDesc);
        //  fill arg: target
        loadThisOrNullIfStatic();
        //  fill arg: method args
        loadArgArray();

        //  invoke
        invokeStatic(ENHANCER_TYPE, ASM_METHOD_WEAVER_ON_BEFORE);

        //  标记 try-catch-finally 代码段起始位置
        mark(beginLabel);
    }

    /**
     * invoke {@link MethodEnhancer#methodOnReturning(Object)} by asm
     *
     * @param opcode one of {@link Opcodes#RETURN}, {@link Opcodes#IRETURN}, {@link Opcodes#FRETURN},
     *               {@link Opcodes#ARETURN}, {@link Opcodes#LRETURN}, {@link Opcodes#DRETURN} or {@link
     *               Opcodes#ATHROW}.
     */
    @Override
    protected void onMethodExit(int opcode) {
        if (ATHROW != opcode) {
            //  根据返回码获取返回值
            //  fill arg: return value
            loadReturn(opcode);
            invokeStatic(ENHANCER_TYPE, ASM_METHOD_WEAVER_ON_RETURNING);
        }
    }

    /**
     * invoke {@link MethodEnhancer#methodOnThrowing(Throwable)} by asm
     *
     * @param maxStack  maximum stack size of the method.
     * @param maxLocals maximum number of local variables for the method.
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        //  在visitEnd之前调用，构造try-catch代码块
        mark(endLabel);
        //  beginLabel 与 endLabel 之间使用try-catch
        catchException(beginLabel, endLabel, THROWABLE_TYPE);
        //  从栈顶加载异常
        //  fill arg: throwable
        dup();
        invokeStatic(ENHANCER_TYPE, ASM_METHOD_WEAVER_ON_THROWING);
        //  抛出原有异常
        throwException();

        super.visitMaxs(maxStack, maxLocals);
    }

    private void loadThisOrNullIfStatic() {
        if (WeaverUtil.isStatic(methodAccess)) {
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
        if (WeaverUtil.isStatic(methodAccess)) {
            //  Class.forName(clazz);
            visitLdcInsn(className);
            invokeStatic(CLASS_TYPE, ASM_METHOD_CLASS_FOR_NAME);
        } else {
            //  this.getClass();
            loadThis();
            invokeVirtual(OBJECT_TYPE, ASM_METHOD_OBJECT_GET_CLASS);
        }
        //  getClassLoader();
        invokeVirtual(CLASS_TYPE, ASM_METHOD_CLASS_GET_CLASS_LOADER);
    }
}
