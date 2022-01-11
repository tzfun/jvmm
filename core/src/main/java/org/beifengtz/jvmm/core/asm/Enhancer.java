package org.beifengtz.jvmm.core.asm;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Description: TODO
 *
 * Created in 14:07 2021/12/17
 *
 * @author beifengtz
 */
public class Enhancer implements ClassFileTransformer {

    private final static Map<Class<?>, Object> classBytesCache = new WeakHashMap<>();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        return new byte[0];
    }

}
