package org.beifengtz.jvmm.log;

import org.beifengtz.jvmm.common.logger.LoggerEvent;
import org.beifengtz.jvmm.common.logger.LoggerLevel;
import org.beifengtz.jvmm.log.printer.Printer;
import org.slf4j.Marker;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description: TODO
 * <p>
 * Created in 16:35 2021/12/9
 *
 * @author beifengtz
 */
public class Logger implements org.slf4j.Logger {

    protected String name;
    protected List<Printer> printers = new ArrayList<>();

    public Logger(String name) {
        this.name = name;
    }

    public void addPrinter(Printer printer) {
        printers.add(printer);
    }

    public void addAllPrinter(List<Printer> printers) {
        this.printers.addAll(printers);
    }

    private String formatPattern(String msg, String pattern, LoggerLevel level, boolean ignoreAnsi) {
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

    private void print(LoggerEvent event) {
        JvmmLogConfiguration config = StaticLoggerBinder.getSingleton().getConfig();
        for (Printer printer : printers) {
            if (printer.preformat()) {
                String msg = String.format(event.getMsg().replaceAll("\\{}", "%s"), event.getArgs());
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return StaticLoggerBinder.getSingleton().getConfig().getLevel().getValue() >= LoggerLevel.TRACE.getValue();
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled()) {
            print(LoggerEvent.create(LoggerLevel.TRACE, msg).setName(name));
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            print(LoggerEvent.create(LoggerLevel.TRACE, format, arg).setName(name));
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            print(LoggerEvent.create(LoggerLevel.TRACE, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            print(LoggerEvent.create(LoggerLevel.TRACE, format, arguments).setName(name));
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) {
            print(LoggerEvent.create(LoggerLevel.TRACE, msg, t).setName(name));
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
        return StaticLoggerBinder.getSingleton().getConfig().getLevel().getValue() >= LoggerLevel.DEBUG.getValue();
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled()) {
            print(LoggerEvent.create(LoggerLevel.DEBUG, msg).setName(name));
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            print(LoggerEvent.create(LoggerLevel.DEBUG, format, arg).setName(name));
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            print(LoggerEvent.create(LoggerLevel.DEBUG, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            print(LoggerEvent.create(LoggerLevel.DEBUG, format, arguments).setName(name));
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            print(LoggerEvent.create(LoggerLevel.DEBUG, msg, t).setName(name));
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
        return StaticLoggerBinder.getSingleton().getConfig().getLevel().getValue() >= LoggerLevel.INFO.getValue();
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            print(LoggerEvent.create(LoggerLevel.INFO, msg).setName(name));
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            print(LoggerEvent.create(LoggerLevel.INFO, format, arg).setName(name));
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            print(LoggerEvent.create(LoggerLevel.INFO, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            print(LoggerEvent.create(LoggerLevel.INFO, format, arguments).setName(name));
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            print(LoggerEvent.create(LoggerLevel.INFO, msg, t).setName(name));
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
        return StaticLoggerBinder.getSingleton().getConfig().getLevel().getValue() >= LoggerLevel.WARN.getValue();
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            print(LoggerEvent.create(LoggerLevel.WARN, msg).setName(name));
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            print(LoggerEvent.create(LoggerLevel.WARN, format, arg).setName(name));
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            print(LoggerEvent.create(LoggerLevel.WARN, format, arguments).setName(name));
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            print(LoggerEvent.create(LoggerLevel.WARN, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) {
            print(LoggerEvent.create(LoggerLevel.WARN, msg, t).setName(name));
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
        return StaticLoggerBinder.getSingleton().getConfig().getLevel().getValue() >= LoggerLevel.ERROR.getValue();
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            print(LoggerEvent.create(LoggerLevel.ERROR, msg).setName(name));
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            print(LoggerEvent.create(LoggerLevel.ERROR, format, arg).setName(name));
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            print(LoggerEvent.create(LoggerLevel.ERROR, format, arg1, arg2).setName(name));
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            print(LoggerEvent.create(LoggerLevel.ERROR, format, arguments).setName(name));
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            print(LoggerEvent.create(LoggerLevel.ERROR, msg, t).setName(name));
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
}
