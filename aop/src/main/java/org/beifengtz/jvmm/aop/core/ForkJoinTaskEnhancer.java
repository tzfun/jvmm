package org.beifengtz.jvmm.aop.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.beifengtz.jvmm.aop.core.WeaverUtil.ASM_THREAD_LOCAL_STORE_CLONE_ATTRIBUTES;
import static org.beifengtz.jvmm.aop.core.WeaverUtil.ASM_THREAD_LOCAL_STORE_SET_ATTRIBUTES;
import static org.beifengtz.jvmm.aop.core.WeaverUtil.FORK_JOIN_TASK;
import static org.beifengtz.jvmm.aop.core.WeaverUtil.JVMM_ATTRIBUTE_FIELD_NAME;
import static org.beifengtz.jvmm.aop.core.WeaverUtil.THREAD_LOCAL_STORE;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ASM9;

/**
 * description: TODO
 * date: 20:22 2023/10/16
 *
 * @author beifengtz
 */
public class ForkJoinTaskEnhancer extends ClassVisitor {
    protected ForkJoinTaskEnhancer(ClassVisitor cv) {
        super(ASM9, cv);
    }

    public static byte[] enhance(byte[] classBytes) throws IOException {
        ClassReader cr = new ClassReader(new ByteArrayInputStream(classBytes));
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
        //  添加属性字段
        cw.visitField(Opcodes.ACC_PRIVATE, JVMM_ATTRIBUTE_FIELD_NAME, Type.getDescriptor(Attributes.class), null, null);
        cr.accept(new ForkJoinTaskEnhancer(cw), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        //  增强 ForkJoinTask#doExec() 方法
        if ("doExec".equals(name) && "()I".equals(desc)) {
            return new AdviceAdapter(ASM9, mv, access, name, desc) {
                @Override
                protected void onMethodEnter() {
                    loadThis();
                    getField(FORK_JOIN_TASK, JVMM_ATTRIBUTE_FIELD_NAME, Type.getType(Attributes.class));
                    invokeStatic(THREAD_LOCAL_STORE, ASM_THREAD_LOCAL_STORE_SET_ATTRIBUTES);
                }
            };
        }
        //  构造函数中赋值
        else if ("<init>".equals(name) && "()V".equals(desc)) {
            return new AdviceAdapter(ASM9, mv, access, name, desc) {
                @Override
                protected void onMethodExit(int opcode) {
                    loadThis();
                    invokeStatic(THREAD_LOCAL_STORE, ASM_THREAD_LOCAL_STORE_CLONE_ATTRIBUTES);
                    putField(FORK_JOIN_TASK, JVMM_ATTRIBUTE_FIELD_NAME, Type.getType(Attributes.class));
                }
            };
        } else {
            return mv;
        }
    }
}
