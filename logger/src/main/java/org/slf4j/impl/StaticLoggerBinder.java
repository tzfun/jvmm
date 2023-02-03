package org.slf4j.impl;

import org.beifengtz.jvmm.log.JvmmLogConfiguration;
import org.beifengtz.jvmm.log.JvmmLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * @author beifengtz
 * @description: SLF4J的Logger绑定类
 * @date 9:53 2023/2/3
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    private static final String loggerFactoryClassStr = JvmmLoggerFactory.class.getName();
    private final ILoggerFactory loggerFactory;

    private StaticLoggerBinder() {
        loggerFactory = new JvmmLoggerFactory();
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return loggerFactoryClassStr;
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    public JvmmLogConfiguration getConfig() {
        return ((JvmmLoggerFactory) loggerFactory).getConfig();
    }
}
