package org.beifengtz.jvmm.aop;

import java.lang.instrument.Instrumentation;

/**
 * description TODO
 * date 20:45 2023/11/5
 *
 * @author beifengtz
 */
public class JvmmAgent {

    private static volatile Instrumentation globalInstrumentation;

    public static Instrumentation getInstrumentation() {
        return globalInstrumentation;
    }

    public static void premain(final String agentArgs, final Instrumentation inst) throws Exception {
        globalInstrumentation = inst;
        JvmmAOPInitializer.initTracing(inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        globalInstrumentation = inst;
        JvmmAOPInitializer.initTracing(inst);
    }
}
