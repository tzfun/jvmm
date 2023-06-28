package org.beifengtz.jvmm.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * description: TODO
 * date: 11:54 2023/6/28
 *
 * @author beifengtz
 */
public class Enhancer implements ClassFileTransformer {

    public static byte[] enhanceMethod(Class<?> targetClass, MethodListener methodListener) throws IOException {
        return enhanceMethod(targetClass, methodListener, null);
    }

    public static byte[] enhanceMethod(Class<?> targetClass, MethodListener methodListener, String methodRegex) throws IOException {
        return enhanceMethod(targetClass.getName(), methodListener, methodRegex);
    }

    public static byte[] enhanceMethod(String className, MethodListener methodListener, String methodRegex) throws IOException {
        return enhanceMethod(className, (cn, mn) -> methodListener, methodRegex);
    }

    /**
     * @param className                 目标类全路径，例如：org.beifengtz.jvmm.asm.Enhancer
     * @param methodListenerConstructor {@link MethodListenerConstructor}，根据 className 和 methodName 构造{@link MethodListener}
     * @param methodRegex               方法名匹配正则表达式，例如 to.*String 匹配 toString, toPanelString, toJsonString等，
     *                                  如果为 null则表示全匹配，即默认 .*
     * @return 增强后类的 class bytes
     * @throws IOException 读取类字节码失败时抛出异常
     */
    public static byte[] enhanceMethod(String className, MethodListenerConstructor methodListenerConstructor,
                                       String methodRegex) throws IOException {
        ClassReader cr = new ClassReader(className);
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
        cr.accept(new MethodEnhancer(methodListenerConstructor, className, cw, methodRegex), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    public static byte[] enhanceMethod(byte[] classBytes, String className, MethodListener methodListener,
                                       String methodRegex) throws IOException {
        return enhanceMethod(classBytes, className, (cn, mn) -> methodListener, methodRegex);
    }

    public static byte[] enhanceMethod(byte[] classBytes, String className, MethodListenerConstructor methodListenerConstructor,
                                       String methodRegex) throws IOException {
        ClassReader cr = new ClassReader(new ByteArrayInputStream(classBytes));
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);
        cr.accept(new MethodEnhancer(methodListenerConstructor, className, cw, methodRegex), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private final String classRegex;
    private final String methodRegex;
    private final MethodListenerConstructor methodListenerConstructor;

    public Enhancer(String classRegex, String methodRegex, MethodListener methodListener) {
        this(classRegex, methodRegex, (cn, mn) -> methodListener);
    }

    public Enhancer(String classRegex, String methodRegex, MethodListenerConstructor methodListenerConstructor) {
        this.classRegex = classRegex;
        this.methodRegex = methodRegex;
        this.methodListenerConstructor = methodListenerConstructor;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        if (className.matches(classRegex)) {
            try {
                return enhanceMethod(classfileBuffer, className, methodListenerConstructor, methodRegex);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
