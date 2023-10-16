package org.beifengtz.jvmm.aop.core.weaver;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.ASM_UTILS_WRAP_RUNNABLE;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.WRAPPER_UTILS;

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
    public ExecutorWeaver(MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        super(ASM9, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        loadArg(0);
        //  Utils.wrap(Runnable)
        invokeStatic(WRAPPER_UTILS, ASM_UTILS_WRAP_RUNNABLE);
        storeArg(0);
    }
}
