package org.beifengtz.jvmm.asm.test;

import org.beifengtz.jvmm.asm.Enhancer;
import org.beifengtz.jvmm.asm.MethodInfo;
import org.beifengtz.jvmm.asm.MethodListener;
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
            public void after(MethodInfo info, Object returnValue) throws Throwable {
                System.out.println("method after: " + info.getClassName() + "#" + info.getMethodName());
            }

            @Override
            public void error(MethodInfo info, Throwable throwable) throws Throwable {
                System.out.println("method error: " + info.getClassName() + "#" + info.getMethodName() + " " + throwable);
            }
        }, "say.*");

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
        String filter = "s*y";
        String methodRegex = filter.replaceAll("\\*", ".*");
        System.out.println("say".matches(methodRegex));;
        System.out.println("<init>".matches(methodRegex));
        System.out.println("sayhello".matches(methodRegex));
    }

}
