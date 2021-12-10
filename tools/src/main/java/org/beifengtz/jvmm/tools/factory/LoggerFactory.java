package org.beifengtz.jvmm.tools.factory;

import org.beifengtz.jvmm.tools.logger.DefaultEmptyLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * Description: TODO
 *
 * Created in 15:49 2021/12/9
 *
 * @author beifengtz
 */
public class LoggerFactory {

    private static final DefaultEmptyLogger DEFAULT_EMPTY_LOGGER = new DefaultEmptyLogger();
    private static ILoggerFactory CUSTOM_LOGGER_FACTORY;

    public static synchronized void register(ILoggerFactory loggerFactory) {
        CUSTOM_LOGGER_FACTORY = loggerFactory;
    }

    public static Logger logger() {
        return logger("org.beifengtz.jvmm.Logger");
    }

    public static Logger logger(Class<?> clazz) {
        return logger(clazz.getName());
    }

    public static Logger logger(String name) {
        if (CUSTOM_LOGGER_FACTORY == null) {
            return DEFAULT_EMPTY_LOGGER;
        } else {
            return CUSTOM_LOGGER_FACTORY.getLogger(name);
        }
    }
}
