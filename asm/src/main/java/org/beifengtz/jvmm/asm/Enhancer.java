package org.beifengtz.jvmm.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * description: TODO
 * date: 11:54 2023/6/28
 *
 * @author beifengtz
 */
public class Enhancer {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    public static byte[] enhance(Class<?> targetClass, MethodListener methodListener) throws IOException {
        ClassReader cr = new ClassReader(targetClass.getName());
        // 字节码增强
        final ClassWriter cw = new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS);

        cr.accept(new MethodWeaver(ID_GENERATOR.getAndIncrement(), methodListener, targetClass, cw), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

}
