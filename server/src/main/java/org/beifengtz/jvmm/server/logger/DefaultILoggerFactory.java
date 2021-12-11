package org.beifengtz.jvmm.server.logger;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Description: TODO
 *
 * Created in 16:35 2021/12/9
 *
 * @author beifengtz
 */
public class DefaultILoggerFactory extends InternalLoggerFactory implements ILoggerFactory {

    static volatile DefaultILoggerFactory INSTANCE = null;
    static ConcurrentHashMap<String, DefaultLoggerAdaptor> loggerMap = new ConcurrentHashMap<>();
    volatile LoggerLevel loggerLevel;

    public static DefaultILoggerFactory newInstance() {
        return newInstance(LoggerLevel.INFO);
    }

    public static DefaultILoggerFactory newInstance(LoggerLevel level) {
        if (INSTANCE == null) {
            synchronized (DefaultILoggerFactory.class) {
                if (INSTANCE != null) {
                    return INSTANCE;
                }
                INSTANCE = new DefaultILoggerFactory();
                INSTANCE.loggerLevel = level;
                return INSTANCE;
            }
        } else {
            return INSTANCE;
        }
    }

    public void setLevel(LoggerLevel level) {
        loggerLevel = level;
    }

    @Override
    public Logger getLogger(String name) {
        return getAdapter(name);
    }

    @Override
    protected InternalLogger newInstance(String name) {
        return getAdapter(name);
    }

    protected DefaultLoggerAdaptor getAdapter(String name) {
        DefaultLoggerAdaptor logger = loggerMap.get(name);
        if (logger != null) {
            return logger;
        } else {
            DefaultLoggerAdaptor newInstance = new DefaultLoggerAdaptor(name);
            DefaultLoggerAdaptor oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }
}
