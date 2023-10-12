package org.beifengtz.jvmm.aop.core;

import org.beifengtz.jvmm.aop.wrapper.WrappedScheduledThreadPoolExecutor;
import org.beifengtz.jvmm.aop.wrapper.WrappedThreadPoolExecutor;
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

    /**
     * 对{@link Executor}进行增强。只能对<strong>非 JDK 实现</strong>的Executor增强，
     * 如果要对 JDK 实现的例如{@link java.util.concurrent.ThreadPoolExecutor} 进行增强，
     * 请直接使用{@link WrappedThreadPoolExecutor} 或 {@link WrappedScheduledThreadPoolExecutor}，
     * 它们本身就是一个增强过的线程池，不需要增强。
     *
     * @param executorClass {@link Executor}的类 {@link Class} 对象，此class中必须有 Override {@link Executor#execute}方法
     * @return 增强后的 class 字节码
     * @throws IOException 读取 class 字节码错误时抛出
     */
    public static byte[] enhance(Class<? extends Executor> executorClass) throws IOException {
        String className = executorClass.getName();

        if (className.startsWith("java.util.concurrent")) {
            throw new IOException("The executor defined in `java.util.concurrent` package cannot be enhanced, " +
                    "please use WrappedThreadPoolExecutor or WrappedScheduledThreadPoolExecutor");
        }
        ClassReader cr = new ClassReader(className);
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
        if (WrappedThreadPoolExecutor.class.isAssignableFrom(executorClass)) {
            return cw.toByteArray();
        }
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
