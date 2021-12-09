package org.beifengtz.jvmm.server.logger;

import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import org.beifengtz.jvmm.tools.logger.DefaultEmptyLogger;
import org.beifengtz.jvmm.tools.logger.LoggerEvent;
import org.beifengtz.jvmm.tools.logger.LoggerLevel;

import static org.beifengtz.jvmm.server.ServerBootstrap.AGENT_BOOT_CLASS;

/**
 * Description: TODO
 *
 * Created in 16:35 2021/12/9
 *
 * @author beifengtz
 */
public class DefaultLoggerAdaptor extends DefaultEmptyLogger implements InternalLogger {

    private final String name;

    private static void publish(LoggerEvent event) {
        try {
            Class<?> bootClazz = Thread.currentThread().getContextClassLoader().loadClass(AGENT_BOOT_CLASS);
            bootClazz.getMethod("logger", String.class, Throwable.class).invoke(null, event.toString(), event.getThrowable());
        } catch (Throwable e) {
            System.err.println("Invoke agent boot method(#logger) failed!");
            e.printStackTrace();
        }
    }

    public DefaultLoggerAdaptor(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return DefaultILoggerFactory.INSTANCE.loggerLevel.getValue() >= LoggerLevel.TRACE.getValue();
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.TRACE, msg).setName(name));
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.TRACE, format, arg).setName(name));
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.TRACE, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.TRACE, format, arguments).setName(name));
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.TRACE, msg, t).setName(name));
        }
    }

    @Override
    public void trace(Throwable t) {
        if (isTraceEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.TRACE, t.getMessage(), t).setName(name));
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return DefaultILoggerFactory.INSTANCE.loggerLevel.getValue() >= LoggerLevel.DEBUG.getValue();
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.DEBUG, msg).setName(name));
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.DEBUG, format, arg).setName(name));
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.DEBUG, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.DEBUG, format, arguments).setName(name));
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.DEBUG, msg, t).setName(name));
        }
    }

    @Override
    public void debug(Throwable t) {
        if (isDebugEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.DEBUG, t.getMessage(), t).setName(name));
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return DefaultILoggerFactory.INSTANCE.loggerLevel.getValue() >= LoggerLevel.INFO.getValue();
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.INFO, msg).setName(name));
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.INFO, format, arg).setName(name));
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.INFO, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.INFO, format, arguments).setName(name));
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.INFO, msg, t).setName(name));
        }
    }

    @Override
    public void info(Throwable t) {
        if (isInfoEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.INFO, t.getMessage(), t).setName(name));
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return DefaultILoggerFactory.INSTANCE.loggerLevel.getValue() >= LoggerLevel.WARN.getValue();
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.WARN, msg).setName(name));
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.WARN, format, arg).setName(name));
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.WARN, format, arguments).setName(name));
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.WARN, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.WARN, msg, t).setName(name));
        }
    }

    @Override
    public void warn(Throwable t) {
        if (isWarnEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.WARN, t.getMessage(), t).setName(name));
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return DefaultILoggerFactory.INSTANCE.loggerLevel.getValue() >= LoggerLevel.ERROR.getValue();
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.ERROR, msg).setName(name));
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.ERROR, format, arg).setName(name));
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.ERROR, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.ERROR, format, arguments).setName(name));
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.ERROR, msg, t).setName(name));
        }
    }

    @Override
    public void error(Throwable t) {
        if (isErrorEnabled()) {
            publish(LoggerEvent.create(LoggerLevel.ERROR, t.getMessage(), t).setName(name));
        }
    }

    @Override
    public boolean isEnabled(InternalLogLevel level) {
        return false;
    }

    @Override
    public void log(InternalLogLevel level, String msg) {
        publish(LoggerEvent.create(levelParseFrom(level), msg).setName(name));
    }

    @Override
    public void log(InternalLogLevel level, String format, Object arg) {
        publish(LoggerEvent.create(levelParseFrom(level), format, arg).setName(name));
    }

    @Override
    public void log(InternalLogLevel level, String format, Object argA, Object argB) {
        publish(LoggerEvent.create(levelParseFrom(level), format, argA, argB).setName(name));
    }

    @Override
    public void log(InternalLogLevel level, String format, Object... arguments) {
        publish(LoggerEvent.create(levelParseFrom(level), format, arguments).setName(name));
    }

    @Override
    public void log(InternalLogLevel level, String msg, Throwable t) {
        publish(LoggerEvent.create(levelParseFrom(level), msg, t).setName(name));
    }

    @Override
    public void log(InternalLogLevel level, Throwable t) {
        publish(LoggerEvent.create(levelParseFrom(level), t.getMessage(), t).setName(name));
    }

    private LoggerLevel levelParseFrom(InternalLogLevel level) {
        if (level == null) {
            return LoggerLevel.OFF;
        }
        switch (level) {
            case INFO:
                return LoggerLevel.INFO;
            case WARN:
                return LoggerLevel.WARN;
            case DEBUG:
                return LoggerLevel.DEBUG;
            case ERROR:
                return LoggerLevel.ERROR;
            case TRACE:
                return LoggerLevel.TRACE;
        }
        return LoggerLevel.OFF;
    }
}
