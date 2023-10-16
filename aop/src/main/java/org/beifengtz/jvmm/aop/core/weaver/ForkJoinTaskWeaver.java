package org.beifengtz.jvmm.aop.core.weaver;

import org.beifengtz.jvmm.aop.core.Attributes;
import org.beifengtz.jvmm.aop.core.MethodEnhancer;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.ASM_METHOD_WEAVER_ON_THROWING;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.ASM_THREAD_LOCAL_STORE_SET_ATTRIBUTES;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.ENHANCER_TYPE;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.FORK_JOIN_TASK;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.JVMM_ATTRIBUTE_FIELD_NAME;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.THREAD_LOCAL_STORE;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.THROWABLE_TYPE;

/**
 * description: 在 ForkJoinTask 类中添加一个属性 {@link WeaverUtil#JVMM_ATTRIBUTE_FIELD_NAME}，此属性保存调用线程的ThreadLocal属性，
 * 在后续执行 doExec 方法时传递给执行线程
 * date: 18:02 2023/10/16
 *
 * @author beifengtz
 */
public class ForkJoinTaskWeaver extends AdviceAdapter {
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
    public ForkJoinTaskWeaver(MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        super(ASM9, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        getField(FORK_JOIN_TASK, JVMM_ATTRIBUTE_FIELD_NAME, Type.getType(Attributes.class));
        dup();
        invokeStatic(THREAD_LOCAL_STORE, ASM_THREAD_LOCAL_STORE_SET_ATTRIBUTES);
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
}
