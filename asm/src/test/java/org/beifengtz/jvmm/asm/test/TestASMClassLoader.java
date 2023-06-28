package org.beifengtz.jvmm.asm.test;

/**
 * description: TODO
 * date: 18:32 2023/6/28
 *
 * @author beifengtz
 */
public class TestASMClassLoader extends ClassLoader {

    private final String className;
    private final byte[] classBytes;

    public TestASMClassLoader(String className, byte[] classBytes, ClassLoader parent) {
        super(parent);
        this.className = className;
        this.classBytes = classBytes;
    }

    public Class<?> defineClass() {
        return super.defineClass(className, classBytes, 0, classBytes.length);
    }
}
