package org.beifengtz.jvmm.aop.core;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

/**
 * description: TODO
 * date: 10:27 2023/10/16
 *
 * @author beifengtz
 */
public class MethodTransformer  implements ClassFileTransformer {

    private final String classRegex;
    private final String classIgnoreRegex;
    private final String methodRegex;
    private final String methodIgnoreRegex;
    private final MethodListener methodListener;
    private final String ignoreListenerRegex;

    /**
     * 增强器构造器
     *
     * @param classPattern        对满足匹配规则的类进行增强，通配符为 *，例如：<pre>org.beifengtz.jvmm.*.*Util</pre>
     * @param classIgnorePattern  不对满足匹配规则的类增强，而是忽略，通配符为 *，例如：<pre>org.beifengtz.jvmm.*.*Factory</pre>
     * @param methodPattern       对满足匹配规则的方法进行增强，同 classPattern
     * @param methodIgnorePattern 不对满足匹配规则的方法增强，而是忽略，同 classIgnorePattern
     * @param methodListener      {@link MethodListener}
     */
    public MethodTransformer(String classPattern, String classIgnorePattern, String methodPattern, String methodIgnorePattern,
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
        //  不对listener自己增强
        this.ignoreListenerRegex = methodListener.getClass().getName().replaceAll("\\.", "\\\\.") + ".*";
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        className = className.replaceAll("/", ".");
        if (needEnhance(className)) {
            try {
                return Enhancer.enhanceMethod(classfileBuffer, className, methodListener, methodRegex, methodIgnoreRegex);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 通过 instrumentation 接口对类进行增强
     *
     * @param instrumentation {@link Instrumentation}对象
     * @throws UnsupportedOperationException retransform failed case
     */
    public void enhance(Instrumentation instrumentation) {
        instrumentation.addTransformer(this, true);
        boolean retransformClassesSupported = instrumentation.isRetransformClassesSupported();
        for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
            String className = clazz.getName();
            if (needEnhance(className)) {
                if (retransformClassesSupported) {
                    try {
                        instrumentation.retransformClasses(clazz);
                    } catch (UnmodifiableClassException e) {
                        throw new UnsupportedOperationException("Can not retransform class by instrumentation: " + className, e);
                    }
                } else {
                    throw new UnsupportedOperationException("Can not retransform class by instrumentation: retransform classes not supported. " +
                            "If you still want to use the enhancement, please enable Can-Retransform-Classes in the manifest to true.");
                }
            }
        }
    }

    private boolean needEnhance(String className) {
        return !className.matches(ignoreListenerRegex) &&
                !className.matches(".*\\$\\d+.*") &&
                !isSelf(className) &&
                className.matches(classRegex) &&
                (classIgnoreRegex == null || !className.matches(classIgnoreRegex));
    }

    private boolean isSelf(String className) {
        return className.startsWith("org.beifengtz.jvmm.aop");
    }

}
