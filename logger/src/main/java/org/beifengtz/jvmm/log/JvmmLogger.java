package org.beifengtz.jvmm.log;

import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import org.beifengtz.jvmm.log.printer.Printer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

/**
 * description: TODO
 * date: 14:49 2023/12/28
 *
 * @author beifengtz
 */
public class JvmmLogger implements InternalLogger {

    private static final String DEFAULT_MESSAGE = "Uncaught exception:";

    protected String name;
    protected List<Printer> printers = new ArrayList<>();

    public JvmmLogger(String name) {
        this.name = name;
    }

    public void addPrinter(Printer printer) {
        printers.add(printer);
    }

    public void addAllPrinter(List<Printer> printers) {
        this.printers.addAll(printers);
    }

    private String formatPattern(String msg, String pattern, InternalLogLevel level, boolean ignoreAnsi) {
        StringBuilder result = new StringBuilder();
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '%') {
                int start = i + 1;
                while (i < chars.length && chars[i] != '{') i++;
                String mode = pattern.substring(start, i);
                int j = i + 1;
                if ("ansi".equalsIgnoreCase(mode)) {
                    int left = 1;
                    while (left != 0) {
                        i++;
                        if (i < chars.length && chars[i] == '{') left++;
                        if (i < chars.length && chars[i] == '}') left--;
                    }
                    String value = formatPattern(msg, pattern.substring(j, i), level, ignoreAnsi);
                    if (chars[i + 1] == '{') {
                        i++;
                        j = i + 1;

                        while (i < chars.length && chars[i] != '}') i++;
                        String ansiCode = pattern.substring(j, i);
                        if (ignoreAnsi) {
                            result.append(value);
                        } else {
                            if (ansiCode.contains("=")) {
                                String[] terms = ansiCode.split(",");
                                String target = value.trim();
                                for (String term : terms) {
                                    String[] kv = term.trim().split("=");
                                    if (kv.length == 2 && kv[0].trim().equalsIgnoreCase(target)) {
                                        ansiCode = kv[1];
                                        break;
                                    }
                                }
                            }
                            result.append("\33[").append(ansiCode).append("m").append(value).append("\33[0m");
                        }
                    } else {
                        result.append(value);
                    }
                } else if ("level".equalsIgnoreCase(mode)) {
                    String levelStr = level.toString();
                    result.append(levelStr);
                    for (int p = levelStr.length(); p < 5; p++) {
                        result.append(" ");
                    }
                } else if ("class".equalsIgnoreCase(mode)) {
                    result.append(this.name);
                } else if ("date".equalsIgnoreCase(mode)) {
                    j = j + 1;
                    while (i < chars.length && chars[i] != '}') i++;
                    result.append(new SimpleDateFormat(pattern.substring(j, i)).format(new Date()));
                } else if ("msg".equalsIgnoreCase(mode)) {
                    result.append(msg);
                } else {
                    result.append(pattern, start, i);
                }
            } else {
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

    private void pushEvent(LoggerEvent event) {
        JvmmLogConfiguration config = JvmmLoggerFactory.getInstance().getConfig();
        InternalLogLevel targetLevel = null;
        for (Entry<String, InternalLogLevel> entry : config.getLevels().entrySet()) {
            if (event.getName().startsWith(entry.getKey())) {
                targetLevel = entry.getValue();
            }
        }

        if (targetLevel != null && levelCode(targetLevel) >= levelCode(level())) {
            return;
        }

        for (Printer printer : printers) {
            if (printer.preformat()) {
                String msg = event.getMsg() == null
                        ? ""
                        : String.format(event.getMsg().replaceAll("\\{}", "%s"), event.getArgs());
                if (event.getThrowable() != null) {
                    msg += ('\n' + throwableToString(event.getThrowable()));
                }
                printer.print(formatPattern(msg, config.getPattern(), event.getType(), printer.ignoreAnsi()));
            } else {
                printer.print(event);
            }
        }
    }

    private String throwableToString(Throwable t) {
        StringWriter writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(writer)) {
            t.printStackTrace(pw);
        }
        return writer.toString();
    }

    public InternalLogLevel level() {
        return JvmmLoggerFactory.getInstance().getConfig().getLevel();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(InternalLogLevel.TRACE);
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.TRACE, msg).setName(name));
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.TRACE, format, arg).setName(name));
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.TRACE, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.TRACE, format, arguments).setName(name));
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.TRACE, msg, t).setName(name));
        }
    }

    @Override
    public void trace(Throwable t) {
        trace(DEFAULT_MESSAGE, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(InternalLogLevel.DEBUG);
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.DEBUG, msg).setName(name));
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.DEBUG, format, arg).setName(name));
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.DEBUG, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.DEBUG, format, arguments).setName(name));
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.DEBUG, msg, t).setName(name));
        }
    }

    @Override
    public void debug(Throwable t) {
        debug(DEFAULT_MESSAGE, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(InternalLogLevel.INFO);
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.INFO, msg).setName(name));
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.INFO, format, arg).setName(name));
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.INFO, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.INFO, format, arguments).setName(name));
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.INFO, msg, t).setName(name));
        }
    }

    @Override
    public void info(Throwable t) {
        info(DEFAULT_MESSAGE, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(InternalLogLevel.WARN);
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.WARN, msg).setName(name));
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.WARN, format, arg).setName(name));
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.WARN, format, arguments).setName(name));
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.WARN, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.WARN, msg, t).setName(name));
        }
    }

    @Override
    public void warn(Throwable t) {
        warn(DEFAULT_MESSAGE, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(InternalLogLevel.ERROR);
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.ERROR, msg).setName(name));
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.ERROR, format, arg).setName(name));
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.ERROR, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.ERROR, format, arguments).setName(name));
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            pushEvent(LoggerEvent.create(InternalLogLevel.ERROR, msg, t).setName(name));
        }
    }

    @Override
    public void error(Throwable t) {
        error(DEFAULT_MESSAGE, t);
    }

    @Override
    public boolean isEnabled(InternalLogLevel level) {
        int target = levelCode(level);
        int limit = levelCode(level());
        return target >= limit;
    }

    @Override
    public void log(InternalLogLevel level, String msg) {
        switch (level) {
            case TRACE:
                trace(msg);
                break;
            case DEBUG:
                debug(msg);
                break;
            case INFO:
                info(msg);
                break;
            case WARN:
                warn(msg);
                break;
            case ERROR:
                error(msg);
                break;
        }
    }

    @Override
    public void log(InternalLogLevel level, String format, Object arg) {
        switch (level) {
            case TRACE:
                trace(format, arg);
                break;
            case DEBUG:
                debug(format, arg);
                break;
            case INFO:
                info(format, arg);
                break;
            case WARN:
                warn(format, arg);
                break;
            case ERROR:
                error(format, arg);
                break;
        }
    }

    @Override
    public void log(InternalLogLevel level, String format, Object argA, Object argB) {
        switch (level) {
            case TRACE:
                trace(format, argA, argB);
                break;
            case DEBUG:
                debug(format, argA, argB);
                break;
            case INFO:
                info(format, argA, argB);
                break;
            case WARN:
                warn(format, argA, argB);
                break;
            case ERROR:
                error(format, argA, argB);
                break;
        }
    }

    @Override
    public void log(InternalLogLevel level, String format, Object... arguments) {
        switch (level) {
            case TRACE:
                trace(format, arguments);
                break;
            case DEBUG:
                debug(format, arguments);
                break;
            case INFO:
                info(format, arguments);
                break;
            case WARN:
                warn(format, arguments);
                break;
            case ERROR:
                error(format, arguments);
                break;
        }
    }

    @Override
    public void log(InternalLogLevel level, String msg, Throwable t) {
        switch (level) {
            case TRACE:
                trace(msg, t);
                break;
            case DEBUG:
                debug(msg, t);
                break;
            case INFO:
                info(msg, t);
                break;
            case WARN:
                warn(msg, t);
                break;
            case ERROR:
                error(msg, t);
                break;
        }
    }

    @Override
    public void log(InternalLogLevel level, Throwable t) {
        switch (level) {
            case TRACE:
                trace(t);
                break;
            case DEBUG:
                debug(t);
                break;
            case INFO:
                info(t);
                break;
            case WARN:
                warn(t);
                break;
            case ERROR:
                error(t);
                break;
        }
    }

    private int levelCode(InternalLogLevel level) {
        switch (level) {
            case TRACE:
                return 1;
            case DEBUG:
                return 2;
            case INFO:
                return 3;
            case WARN:
                return 4;
            case ERROR:
                return 5;
        }
        return 0;
    }
}
