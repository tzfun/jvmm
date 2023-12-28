package org.beifengtz.jvmm.log.printer;

import org.beifengtz.jvmm.log.LoggerEvent;

import java.util.Map;

/**
 * description: TODO
 * date 15:15 2023/2/3
 * @author beifengtz
 */
public class AgentProxyPrinter implements Printer {

    public static final String AGENT_BOOT_CLASS = "org.beifengtz.jvmm.agent.AgentBootStrap";

    @Override
    public void print(Object content) {
        try {
            Class<?> bootClazz = Thread.currentThread().getContextClassLoader().loadClass(AGENT_BOOT_CLASS);
            bootClazz.getMethod("logger", Map.class).invoke(null, ((LoggerEvent) content).toMap());
        } catch (Throwable e) {
            System.err.println("Invoke agent boot method(#logger) failed!");
            e.printStackTrace();
        }
    }

    @Override
    public boolean ignoreAnsi() {
        return true;
    }

    @Override
    public boolean preformat() {
        return false;
    }
}
