package org.beifengtz.jvmm.aop.test;

import org.beifengtz.jvmm.aop.core.Enhancer;
import org.beifengtz.jvmm.aop.core.MethodInfo;
import org.beifengtz.jvmm.aop.core.MethodListener;
import org.junit.jupiter.api.Test;

/**
 * description: TODO
 * date: 18:12 2023/6/28
 *
 * @author beifengtz
 */
public class TestInvoke {

    @Test
    public void test() throws Exception {
        byte[] classBytes = Enhancer.enhanceMethod(People.class, new MethodListener() {
            @Override
            public void before(MethodInfo info) throws Throwable {
                System.out.println("method before: " + info.getClassName() + "#" + info.getMethodName());
            }

            @Override
            public void afterReturning(MethodInfo info, Object returnValue) throws Throwable {
                System.out.println("method after return: " + info.getClassName() + "#" + info.getMethodName());
            }

            @Override
            public void afterThrowing(MethodInfo info, Throwable throwable) throws Throwable {
                System.out.println("method error throw: " + info.getClassName() + "#" + info.getMethodName() + " " + throwable);
            }

            @Override
            public void after(MethodInfo info, Object returnVal, Throwable throwable) throws Throwable {
                System.out.println("method after: " + info.getClassName() + "#" + info.getMethodName());
            }
        }, "say.*", "sayHelloWorld");

        TestASMClassLoader classLoader = new TestASMClassLoader(People.class.getName(), classBytes, Thread.currentThread().getContextClassLoader());
        Class<?> clazz = classLoader.defineClass();
        Object obj = clazz.newInstance();
        clazz.getMethod("say").invoke(obj);
        clazz.getMethod("run").invoke(obj);
        clazz.getMethod("sayHello").invoke(obj);
        clazz.getMethod("sayHelloWorld").invoke(obj);
        clazz.getMethod("say").invoke(obj);
    }

    @Test
    public void testMethodMatch() {
        String excludeRegex = "^((?!<init>|<clinit>).)*$";
        String filter = "s*y*";
        String methodRegex = filter.replaceAll("\\*", ".*");
        System.out.println("say".matches(methodRegex));
        ;
        System.out.println("<init>".matches(methodRegex));
        System.out.println("sayHello".matches(methodRegex));
        System.out.println("sayHelloWorld".matches(methodRegex));
        System.out.println("sayHelloWorld1".matches(methodRegex));
    }

    @Test
    public void testPattern() {
        String classPattern = "org.beifengtz.jvmm.asm.*";
        classPattern = classPattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
        System.out.println(classPattern);
        System.out.println("org.beifengtz.jvmm.asm.Enhancer".matches(classPattern));
        System.out.println("org.beifengtz.jvmm.asm.util.EnhancerUtil".matches(classPattern));
        System.out.println("org.beifengtz.jvmm.util.asm.EnhancerUtil".matches(classPattern));
    }

}
