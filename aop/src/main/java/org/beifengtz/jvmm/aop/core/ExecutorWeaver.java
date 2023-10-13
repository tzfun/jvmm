package org.beifengtz.jvmm.aop.core;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;

import static org.beifengtz.jvmm.aop.core.WeaverUtil.ASM_EXECUTOR_ENHANCER_GET_CONTEXT_ID;
import static org.beifengtz.jvmm.aop.core.WeaverUtil.ACROSS_THREAD_AGENT;
import static org.beifengtz.jvmm.aop.core.WeaverUtil.RUNNABLE_AGENT_TYPE;

/**
 * description TODO
 * date 16:10 2023/9/11
 *
 * @author beifengtz
 */
public class ExecutorWeaver extends AdviceAdapter {

    /**
     * Constructs a new {@link AdviceAdapter}.
     *
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     */
    protected ExecutorWeaver(MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        super(ASM9, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        //  new RunnableAgent(Runnable, String)
        newInstance(RUNNABLE_AGENT_TYPE);
        dup();
        loadArg(0);
        //  AcrossThreadAgent.getContextId()
        invokeStatic(ACROSS_THREAD_AGENT, ASM_EXECUTOR_ENHANCER_GET_CONTEXT_ID);
        invokeConstructor(RUNNABLE_AGENT_TYPE, Method.getMethod("void <init> (Runnable, String)"));
        storeArg(0);
    }
}
