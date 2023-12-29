package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * description: SLF4J的Logger绑定类
 * date 9:53 2023/2/3
 *
 * @author beifengtz
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    private static final String loggerFactoryClassStr = Slf4jJvmmLoggerFactory.class.getName();
    private final ILoggerFactory loggerFactory;

    private StaticLoggerBinder() {
        loggerFactory = new Slf4jJvmmLoggerFactory();
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
}
