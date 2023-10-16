package org.beifengtz.jvmm.aop.core;

import org.beifengtz.jvmm.aop.listener.MethodListener;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * description: 类方法增强工具，对类中指定的方法进行增强。 Powered by ASM
 * date: 11:54 2023/6/28
 *
 * @author beifengtz
 */
public class Enhancer {

    public static byte[] enhanceMethod(Class<?> targetClass, MethodListener methodListener) throws IOException {
        return enhanceMethod(targetClass, methodListener, null, null);
    }

    public static byte[] enhanceMethod(Class<?> targetClass, MethodListener methodListener, String methodRegex) throws IOException {
        return enhanceMethod(targetClass.getName(), methodListener, methodRegex, null);
    }

    public static byte[] enhanceMethod(Class<?> targetClass, MethodListener methodListener, String methodRegex,
                                       String methodIgnoreRegex) throws IOException {
        return enhanceMethod(targetClass.getName(), methodListener, methodRegex, methodIgnoreRegex);
    }

    /**
     * 对 JVM 已加载的类进行增强，如果该类未被加载，可能会执行失败。
     *
     * @param className         目标类全路径，需要求此类已经被 JVM 加载到内存。例如：org.beifengtz.jvmm.asm.Enhancer
     * @param methodListener    {@link MethodListener}
     * @param methodRegex       正则表达式，针对于该类下满足匹配的方法进行增强。如果为 null 则默认为 .* 全匹配
     * @param methodIgnoreRegex 正则表达式，针对于该类下满足匹配的方法忽略，无需增强。如果为 null 则默认不忽略
     * @return 增强后类的 class bytes
     * @throws IOException 读取类字节码失败时抛出异常
     */
    public static byte[] enhanceMethod(String className, MethodListener methodListener, String methodRegex,
                                       String methodIgnoreRegex) throws IOException {
        ClassReader cr = new ClassReader(className);
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
        cr.accept(new MethodEnhancer(methodListener, className, cw, methodRegex, methodIgnoreRegex), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    /**
     * @param classBytes        类字节码 bytes，无需要求 JVM 是否已加载此类
     * @param methodListener    {@link MethodListener}
     * @param methodRegex       正则表达式，针对于该类下满足匹配的方法进行增强。如果为 null 则默认为 .* 全匹配
     * @param methodIgnoreRegex 正则表达式，针对于该类下满足匹配的方法忽略，无需增强。如果为 null 则默认不忽略
     * @return 增强后类的 class bytes
     * @throws IOException 读取类字节码失败时抛出异常
     */
    public static byte[] enhanceMethod(byte[] classBytes, String className, MethodListener methodListener,
                                       String methodRegex, String methodIgnoreRegex) throws IOException {
        ClassReader cr = new ClassReader(new ByteArrayInputStream(classBytes));
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
        cr.accept(new MethodEnhancer(methodListener, className, cw, methodRegex, methodIgnoreRegex), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    /**
     * 对某个 <strong>非 JDK 实现</strong>的Executor进行增强，且此class中必须有 Override {@link Executor#execute}方法。
     *
     * @param executorClass 被增强类的{@link Class}
     * @return 增强后的字节码
     * @throws IOException 找不到该Class或加载失败时抛出异常
     */
    public static byte[] enhanceExecutor(Class<? extends Executor> executorClass) throws IOException {
        return ExecutorEnhancer.enhance(executorClass);
    }

    /**
     * 对某个 Executor进行增强，且此class中必须有 Override {@link Executor#execute}方法。
     * 注意！如果你增强的是jdk的类，需要在加载它之前进行增强
     *
     * @param className 类名，例如：java.util.concurrent.ThreadPoolExecutor
     * @param bytes     类字节码
     * @return 增强后的字节码
     * @throws IOException 读取或操作类数据失败时抛出此异常
     */
    public static byte[] enhanceExecutor(String className, byte[] bytes) throws IOException {
        return ExecutorEnhancer.enhance(className, bytes);
    }
}
