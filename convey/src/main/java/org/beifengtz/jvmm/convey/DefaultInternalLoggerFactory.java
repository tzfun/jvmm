package org.beifengtz.jvmm.convey;

import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.beifengtz.jvmm.common.logger.DefaultImplLogger;
import org.beifengtz.jvmm.log.LoggerLevel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 3:33 下午 2021/12/11
 *
 * @author beifengtz
 */
public class DefaultInternalLoggerFactory extends InternalLoggerFactory {

    private static DefaultInternalLoggerFactory INSTANCE = null;
    private static final Map<String, InternalLogger> loggerMap = new ConcurrentHashMap<>();
    static LoggerLevel level = LoggerLevel.INFO;

    private DefaultInternalLoggerFactory(){}

    public static DefaultInternalLoggerFactory newInstance() {
        return newInstance(LoggerLevel.INFO);
    }

    public static DefaultInternalLoggerFactory newInstance(LoggerLevel level) {
        if (INSTANCE == null) {
            synchronized (DefaultInternalLoggerFactory.class) {
                if (INSTANCE != null) {
                    return INSTANCE;
                }
                INSTANCE = new DefaultInternalLoggerFactory();
                setLevel(level);
                return INSTANCE;
            }
        } else {
            return INSTANCE;
        }
    }

    private static void setLevel(LoggerLevel lvl) {
        level = lvl;
    }

    @Override
    protected InternalLogger newInstance(String name) {
        InternalLogger logger = loggerMap.get(name);
        if (logger != null) {
            return logger;
        }
        logger = new DefaultInternalLogger(name);
        loggerMap.put(name, logger);
        return logger;
    }

    static class DefaultInternalLogger extends DefaultImplLogger implements InternalLogger {

        private final String name;

        public DefaultInternalLogger(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void trace(Throwable t) {
            super.trace(t.getMessage(), t);
        }

        @Override
        public void debug(Throwable t) {
            super.debug(t.getMessage(), t);
        }

        @Override
        public void info(Throwable t) {
            super.info(t.getMessage(), t);
        }

        @Override
        public void warn(Throwable t) {
            super.warn(t.getMessage(), t);
        }

        @Override
        public void error(Throwable t) {
            super.error(t.getMessage(), t);
        }

        @Override
        public boolean isEnabled(InternalLogLevel level) {
            return true;
        }

        @Override
        public void log(InternalLogLevel level, String msg) {
            switch (level) {
                case INFO:
                    info(msg);
                case WARN:
                    warn(msg);
                case DEBUG:
                    debug(msg);
                case ERROR:
                    error(msg);
                case TRACE:
                    trace(msg);
            }
        }

        @Override
        public void log(InternalLogLevel level, String format, Object arg) {
            switch (level) {
                case INFO:
                    info(format, arg);
                case WARN:
                    warn(format, arg);
                case DEBUG:
                    debug(format, arg);
                case ERROR:
                    error(format, arg);
                case TRACE:
                    trace(format, arg);
            }
        }

        @Override
        public void log(InternalLogLevel level, String format, Object argA, Object argB) {
            switch (level) {
                case INFO:
                    info(format, argA, argB);
                case WARN:
                    warn(format, argA, argB);
                case DEBUG:
                    debug(format, argA, argB);
                case ERROR:
                    error(format, argA, argB);
                case TRACE:
                    trace(format, argA, argB);
            }
        }

        @Override
        public void log(InternalLogLevel level, String format, Object... arguments) {
            switch (level) {
                case INFO:
                    info(format, arguments);
                case WARN:
                    warn(format, arguments);
                case DEBUG:
                    debug(format, arguments);
                case ERROR:
                    error(format, arguments);
                case TRACE:
                    trace(format, arguments);
            }
        }

        @Override
        public void log(InternalLogLevel level, String msg, Throwable t) {
            switch (level) {
                case INFO:
                    info(msg, t);
                case WARN:
                    warn(msg, t);
                case DEBUG:
                    debug(msg, t);
                case ERROR:
                    error(msg, t);
                case TRACE:
                    trace(msg, t);
            }
        }

        @Override
        public void log(InternalLogLevel level, Throwable t) {
            switch (level) {
                case INFO:
                    info(t);
                case WARN:
                    warn(t);
                case DEBUG:
                    debug(t);
                case ERROR:
                    error(t);
                case TRACE:
                    trace(t);
            }
        }
    }
}
