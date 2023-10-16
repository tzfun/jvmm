package org.beifengtz.jvmm.aop.core;

import org.beifengtz.jvmm.aop.core.weaver.ForkJoinTaskWeaver;
import org.beifengtz.jvmm.aop.core.weaver.WeaverUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.ASM_THREAD_LOCAL_STORE_CLONE_ATTRIBUTES;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.FORK_JOIN_TASK;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.JVMM_ATTRIBUTE_FIELD_NAME;
import static org.beifengtz.jvmm.aop.core.weaver.WeaverUtil.THREAD_LOCAL_STORE;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * description: TODO
 * date: 20:22 2023/10/16
 *
 * @author beifengtz
 */
public class ForkJoinTaskEnhancer extends ClassVisitor implements Opcodes {
    private boolean fieldAbsent = true;

    protected ForkJoinTaskEnhancer(ClassVisitor cv) {
        super(ASM9, cv);
    }

    public static byte[] enhance(byte[] classBytes) throws IOException {
        ClassReader cr = new ClassReader(new ByteArrayInputStream(classBytes));
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
        cr.accept(new ForkJoinTaskEnhancer(cw), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (name.equals(JVMM_ATTRIBUTE_FIELD_NAME)) {
            fieldAbsent = false;
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitEnd() {
        if (fieldAbsent) {
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE & Opcodes.ACC_FINAL, JVMM_ATTRIBUTE_FIELD_NAME,
                    "Ljava/lang/Object;", null, null);
            if (fv != null) {
                fv.visitEnd();
            }
        }
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("doExec".equals(name) && "()I".equals(desc)) {
            return new ForkJoinTaskWeaver(mv, access, name, desc, signature, exceptions);
        } else if ("<init>".equals(name) && "()V".equals(desc)) {
            return new AdviceAdapter(ASM9, mv, access, name, desc) {
                @Override
                protected void onMethodExit(int opcode) {
                    invokeStatic(THREAD_LOCAL_STORE, ASM_THREAD_LOCAL_STORE_CLONE_ATTRIBUTES);
                    putField(FORK_JOIN_TASK, JVMM_ATTRIBUTE_FIELD_NAME, WeaverUtil.OBJECT_TYPE);
                }
            };
        } else {
            return mv;
        }
    }
}
