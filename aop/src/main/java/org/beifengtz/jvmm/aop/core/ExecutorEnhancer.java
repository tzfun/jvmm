package org.beifengtz.jvmm.aop.core;

import org.beifengtz.jvmm.aop.agent.EnhancedThreadPoolExecutor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.concurrent.Executor;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * description TODO
 * date 15:24 2023/9/11
 *
 * @author beifengtz
 */
public class ExecutorEnhancer extends ClassVisitor implements Opcodes {
    private static final ThreadLocal<String> CONTEXT_ID_THREAD_LOCAL = new ThreadLocal<>();

    protected ExecutorEnhancer(ClassVisitor cv) {
        super(ASM9, cv);
    }

    /**
     * 获取调用线程的上下文ID
     *
     * @return 上下文ID
     */
    public static String getContextId() {
        return CONTEXT_ID_THREAD_LOCAL.get();
    }

    /**
     * 设置调用线程的上下文ID
     *
     * @param id 上下文ID
     */
    public static void setContextId(String id) {
        CONTEXT_ID_THREAD_LOCAL.set(id);
    }

    public static byte[] enhance(Class<? extends Executor> executorClass) throws IOException {
        String className = executorClass.getName();
        if (EnhancedThreadPoolExecutor.class.isAssignableFrom(executorClass)) {
            throw new IOException("Can not enhance class " + EnhancedThreadPoolExecutor.class.getName());
        }
        if (className.startsWith("java.util.concurrent")) {
            throw new IOException("The executor defined in `java.util.concurrent` package cannot be enhanced, please use EnhancedThreadPoolExecutor");
        }
        ClassReader cr = new ClassReader(className);
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
        cr.accept(new ExecutorEnhancer(cw), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("execute".equals(name) && "(Ljava/lang/Runnable;)V".equals(desc)) {
            return new ExecutorWeaver(mv, access, name, desc, signature, exceptions);
        } else {
            return mv;
        }
    }
}
