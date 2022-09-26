package org.beifengtz.jvmm.core.jad;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:20 2022/9/22
 *
 * @author beifengtz
 */
public class JadUtil {

    public static String decompile(File file, String methodName) throws Exception {
        final StringBuilder result = new StringBuilder();

        OutputSinkFactory sink = new OutputSinkFactory() {
            @Override
            public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> collection) {
                return Arrays.asList(SinkClass.STRING, SinkClass.DECOMPILED, SinkClass.DECOMPILED_MULTIVER, SinkClass.EXCEPTION_MESSAGE);
            }

            @Override
            public <T> Sink<T> getSink(final SinkType sinkType, SinkClass sinkClass) {
                return sinkable -> {
                    // skip message like: Analysing type demo.MathGame
                    if (sinkType == SinkType.PROGRESS) {
                        return;
                    }
                    result.append(sinkable);
                };
            }
        };

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("showversion", "false");
        if (methodName != null && !methodName.trim().isEmpty()) {
            options.put("methodname", methodName);
        }

        CfrDriver driver = new CfrDriver.Builder().withOptions(options).withOutputSink(sink).build();
        driver.analyse(Collections.singletonList(file.getAbsolutePath()));
        return result.toString();
    }

    public static byte[] toBytes(Instrumentation inst, String className) throws Exception {
        return toBytes(inst, className, Thread.currentThread().getContextClassLoader());
    }

    public static byte[] toBytes(Instrumentation inst, String className, ClassLoader classloader) throws Exception {
        Class<?> clazz = Class.forName(className, false, classloader);
        ByteTransformer transformer = new ByteTransformer(clazz);
        reTransformClass(inst, transformer, clazz);
        return transformer.getData();
    }

    public static void reTransformClass(Instrumentation inst, ClassFileTransformer transformer, Class<?>... classes) throws Exception {
        try {
            inst.addTransformer(transformer, true);
            inst.retransformClasses(classes);
        } finally {
            inst.removeTransformer(transformer);
        }
    }

    static class ByteTransformer implements ClassFileTransformer {
        private final Class<?> clazz;

        private byte[] data;

        public ByteTransformer(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (clazz.equals(classBeingRedefined)) {
                data = classfileBuffer;
            }
            return null;
        }

        public byte[] getData() {
            return data;
        }
    }
}
