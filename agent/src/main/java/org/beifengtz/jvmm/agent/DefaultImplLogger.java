package org.beifengtz.jvmm.agent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Description: TODO
 * <p>
 * Created in 15:53 2021/12/9
 *
 * @author beifengtz
 */
public class DefaultImplLogger {

    protected static String TRACE_PATTERN = "[\33[36;1mJvmm\33[30;0m] [\33[35;1mTrace\33[30;0m] %s%n";
    protected static String DEBUG_PATTERN = "[\33[36;1mJvmm\33[30;0m] [\33[34;1mDebug\33[30;0m] %s%n";
    protected static String WARN_PATTERN = "[\33[36;1mJvmm\33[30;0m] [\33[33;1mWarn \33[30;0m] %s%n";
    protected static String INFO_PATTERN = "[\33[36;1mJvmm\33[30;0m] [\33[32;1mInfo \33[30;0m] %s%n";
    protected static String ERROR_PATTERN = "[\33[36;1mJvmm\33[30;0m] [\33[31;1mError\33[30;0m] %s%n";

    static {
        if (Charset.defaultCharset() != StandardCharsets.UTF_8) {
            TRACE_PATTERN = TRACE_PATTERN.replaceAll("\33\\[3.;.m", "");
            DEBUG_PATTERN = DEBUG_PATTERN.replaceAll("\33\\[3.;.m", "");
            WARN_PATTERN = WARN_PATTERN.replaceAll("\33\\[3.;.m", "");
            INFO_PATTERN = INFO_PATTERN.replaceAll("\33\\[3.;.m", "");
            ERROR_PATTERN = ERROR_PATTERN.replaceAll("\33\\[3.;.m", "");
        }
    }

    public boolean isTraceEnabled() {
        return false;
    }

    public void trace(String msg) {
        if (isTraceEnabled()) {
            System.out.format(TRACE_PATTERN, msg);
        }
    }

    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(TRACE_PATTERN, f), arg);
        }
    }

    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(TRACE_PATTERN, f), arg1, arg2);
        }
    }

    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(TRACE_PATTERN, f), arguments);
        }
    }

    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) {
            System.out.format(TRACE_PATTERN, msg);
            t.printStackTrace();
        }
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public void debug(String msg) {
        if (isDebugEnabled()) {
            System.out.format(DEBUG_PATTERN, msg);
        }
    }

    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(DEBUG_PATTERN, f), arg);
        }
    }

    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(DEBUG_PATTERN, f), arg1, arg2);
        }
    }

    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(DEBUG_PATTERN, f), arguments);
        }
    }

    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            System.out.format(DEBUG_PATTERN, msg);
            t.printStackTrace();
        }
    }

    public boolean isInfoEnabled() {
        return true;
    }


    public void info(String msg) {
        if (isInfoEnabled()) {
            System.out.format(INFO_PATTERN, msg);
        }
    }


    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(INFO_PATTERN, f), arg);
        }
    }


    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(INFO_PATTERN, f), arg1, arg2);
        }
    }


    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(INFO_PATTERN, f), arguments);
        }
    }


    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            System.out.format(INFO_PATTERN, msg);
            t.printStackTrace();
        }
    }

    public boolean isWarnEnabled() {
        return true;
    }


    public void warn(String msg) {
        if (isWarnEnabled()) {
            System.out.format(WARN_PATTERN, msg);
        }
    }


    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(WARN_PATTERN, f), arg);
        }
    }


    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(WARN_PATTERN, f), arguments);
        }
    }


    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(WARN_PATTERN, f), arg1, arg2);
        }
    }

    public boolean isErrorEnabled() {
        return true;
    }


    public void error(String msg) {
        if (isErrorEnabled()) {
            System.out.format(ERROR_PATTERN, msg);
        }
    }


    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(ERROR_PATTERN, f), arg);
        }
    }


    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(ERROR_PATTERN, f), arg1, arg2);
        }
    }


    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            String f = format.replaceAll("\\{}", "%s");
            System.out.format(String.format(ERROR_PATTERN, f), arguments);
        }
    }


    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            System.out.format(ERROR_PATTERN, msg);
            t.printStackTrace();
        }
    }

}
