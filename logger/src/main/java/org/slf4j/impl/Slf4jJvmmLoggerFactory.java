package org.slf4j.impl;

import org.beifengtz.jvmm.log.JvmmLogger;
import org.beifengtz.jvmm.log.JvmmLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * description: TODO
 * date: 11:38 2023/12/29
 *
 * @author beifengtz
 */
public class Slf4jJvmmLoggerFactory extends JvmmLoggerFactory implements ILoggerFactory {

    public Slf4jJvmmLoggerFactory() {
        super();
    }

    @Override
    protected JvmmLogger newInstance(String name) {
        JvmmLogger logger = loggerMap.get(name);
        if (logger != null) {
            return logger;
        } else {
            JvmmLogger newInstance = new Sl4jJvmmLogger(name);
            newInstance.addAllPrinter(printers);
            JvmmLogger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }

    @Override
    public Logger getLogger(String name) {
        return (Sl4jJvmmLogger) newInstance(name);
    }
}
