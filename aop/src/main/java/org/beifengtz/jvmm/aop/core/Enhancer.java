package org.beifengtz.jvmm.aop.core;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * description: 类方法增强工具，对类中指定的方法进行增强。 Powered by ASM
 * date: 11:54 2023/6/28
 *
 * @author beifengtz
 */
public class Enhancer implements ClassFileTransformer {

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

    private final String classRegex;
    private final String classIgnoreRegex;
    private final String methodRegex;
    private final String methodIgnoreRegex;
    private final MethodListener methodListener;

    /**
     * 增强器构造器
     *
     * @param classPattern        对满足匹配规则的类进行增强，通配符为 *，例如：<pre>org.beifengtz.jvmm.*.*Util</pre>
     * @param classIgnorePattern  不对满足匹配规则的类增强，而是忽略，通配符为 *，例如：<pre>org.beifengtz.jvmm.*.*Factory</pre>
     * @param methodPattern       对满足匹配规则的方法进行增强，同 classPattern
     * @param methodIgnorePattern 不对满足匹配规则的方法增强，而是忽略，同 classIgnorePattern
     * @param methodListener      {@link MethodListener}
     */
    public Enhancer(@NotNull String classPattern,
                    @Nullable String classIgnorePattern,
                    @Nullable String methodPattern,
                    @Nullable String methodIgnorePattern,
                    MethodListener methodListener) {
        if (classPattern == null) {
            throw new NullPointerException("classRegex can not be null");
        }

        this.classRegex = classPattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
        this.classIgnoreRegex = classIgnorePattern == null
                ? null
                : classIgnorePattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");

        this.methodRegex = methodPattern == null ? ".*" : methodPattern.replaceAll("\\*", ".*");
        this.methodIgnoreRegex = methodIgnorePattern == null
                ? null
                : methodIgnorePattern.replaceAll("\\*", ".*");
        this.methodListener = methodListener;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        className = className.replaceAll("/", ".");
        if (className.matches(classRegex) && (classIgnoreRegex == null || !className.matches(classIgnoreRegex))) {
            try {
                return enhanceMethod(classfileBuffer, className, methodListener, methodRegex, methodIgnoreRegex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 通过 instrumentation 接口对类进行增强
     *
     * @param instrumentation {@link Instrumentation}对象
     * @throws UnmodifiableClassException retransform failed case
     */
    public void enhance(Instrumentation instrumentation) throws UnmodifiableClassException {
        instrumentation.addTransformer(this);
        for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
            String className = clazz.getName();
            if (className.matches(classRegex) && (classIgnoreRegex == null || !className.matches(classIgnoreRegex))) {
                if (instrumentation.isRetransformClassesSupported()) {
                    instrumentation.retransformClasses(clazz);
                } else {
                    throw new UnsupportedOperationException("Can not retransform class by instrumentation: retransform classes not supported. " +
                            "If you still want to use the enhancement, please enable Can-Retransform-Classes in the manifest to true.");
                }
            }
        }
    }
}
