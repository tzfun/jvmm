package org.beifengtz.jvmm.common.logger;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Description: TODO
 * <p>
 * Created in 15:53 2021/12/9
 *
 * @author beifengtz
 */
public class DefaultImplLogger implements Logger {

    protected static final String TRACE_PREFIX = "[Jvmm] [Trace] ";
    protected static final String DEBUG_PREFIX = "[Jvmm] [Debug] ";
    protected static final String WARN_PREFIX = "[Jvmm] [Warn ] ";
    protected static final String INFO_PREFIX = "[Jvmm] [Info ] ";
    protected static final String ERROR_PREFIX = "[Jvmm] [Warn] ";

    protected LoggerLevel level = LoggerLevel.INFO;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isTraceEnabled() {
        return level.getValue() >= LoggerLevel.TRACE.getValue();
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled()) {
            System.out.println(TRACE_PREFIX + msg);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(TRACE_PREFIX + String.format(f, arg));
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(TRACE_PREFIX + String.format(f, arg1, arg2));
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(TRACE_PREFIX + String.format(f, arguments));
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) {
            System.out.println(TRACE_PREFIX + msg);
            t.printStackTrace();
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {

    }

    @Override
    public void trace(Marker marker, String format, Object arg) {

    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return level.getValue() >= LoggerLevel.DEBUG.getValue();
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled()) {
            System.out.println(DEBUG_PREFIX + msg);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(DEBUG_PREFIX + String.format(f, arg));
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(DEBUG_PREFIX + String.format(f, arg1, arg2));
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(DEBUG_PREFIX + String.format(f, arguments));
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            System.out.println(DEBUG_PREFIX + msg);
            t.printStackTrace();
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(Marker marker, String msg) {

    }

    @Override
    public void debug(Marker marker, String format, Object arg) {

    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isInfoEnabled() {
        return level.getValue() >= LoggerLevel.INFO.getValue();
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            System.out.println(INFO_PREFIX + msg);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(INFO_PREFIX + String.format(f, arg));
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(INFO_PREFIX + String.format(f, arg1, arg2));
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(INFO_PREFIX + String.format(f, arguments));
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            System.out.println(INFO_PREFIX + msg);
            t.printStackTrace();
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public void info(Marker marker, String msg) {

    }

    @Override
    public void info(Marker marker, String format, Object arg) {

    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isWarnEnabled() {
        return level.getValue() >= LoggerLevel.WARN.getValue();
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            System.out.println(WARN_PREFIX + msg);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(WARN_PREFIX + String.format(f, arg));
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(WARN_PREFIX + String.format(f, arguments));
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(WARN_PREFIX + String.format(f, arg1, arg2));
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) {
            System.out.println(WARN_PREFIX + msg);
            t.printStackTrace();
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public void warn(Marker marker, String msg) {

    }

    @Override
    public void warn(Marker marker, String format, Object arg) {

    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {

    }

    @Override
    public boolean isErrorEnabled() {
        return level.getValue() >= LoggerLevel.ERROR.getValue();
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            System.out.println(ERROR_PREFIX + msg);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(ERROR_PREFIX + String.format(f, arg));
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(ERROR_PREFIX + String.format(f, arg1, arg2));
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.println(ERROR_PREFIX + String.format(f, arguments));
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            System.out.println(ERROR_PREFIX + msg);
            t.printStackTrace();
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public void error(Marker marker, String msg) {

    }

    @Override
    public void error(Marker marker, String format, Object arg) {

    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {

    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {

    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {

    }

    public void setLevel(LoggerLevel level) {
        this.level = level;
    }
}
