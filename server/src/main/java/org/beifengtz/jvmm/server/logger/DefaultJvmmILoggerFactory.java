package org.beifengtz.jvmm.server.logger;

import org.beifengtz.jvmm.common.logger.DefaultImplLogger;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * Description: TODO
 *
 * Created in 17:35 2021/12/15
 *
 * @author beifengtz
 */
public class DefaultJvmmILoggerFactory implements ILoggerFactory {

    static final DefaultImplLogger DEFAULT_IMPL_LOGGER = new DefaultImplLogger();
    static volatile DefaultJvmmILoggerFactory INSTANCE = null;

    public static DefaultJvmmILoggerFactory newInstance() {
        return newInstance(LoggerLevel.INFO);
    }

    public static DefaultJvmmILoggerFactory newInstance(LoggerLevel level) {
        if (INSTANCE == null) {
            synchronized (DefaultILoggerFactory.class) {
                if (INSTANCE != null) {
                    return INSTANCE;
                }
                INSTANCE = new DefaultJvmmILoggerFactory();
                DEFAULT_IMPL_LOGGER.setLevel(level);
                return INSTANCE;
            }
        } else {
            return INSTANCE;
        }
    }

    @Override
    public Logger getLogger(String name) {
        return DEFAULT_IMPL_LOGGER;
    }
}
