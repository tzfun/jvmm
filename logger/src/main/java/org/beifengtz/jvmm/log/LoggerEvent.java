package org.beifengtz.jvmm.log;

import io.netty.util.internal.logging.InternalLogLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: TODO
 * <p>
 * Created in 15:53 2021/12/9
 *
 * @author beifengtz
 */
public class LoggerEvent {

    private InternalLogLevel level;
    private String msg;
    private Object[] args;
    private Throwable throwable;
    private String name;

    public static LoggerEvent create(InternalLogLevel level, String msg) {
        LoggerEvent info = new LoggerEvent();
        info.level = level;
        info.msg = msg;
        return info;
    }

    public static LoggerEvent create(InternalLogLevel level, String format, Object... args) {
        LoggerEvent info = new LoggerEvent();
        info.level = level;
        info.msg = format;
        info.args = args;
        return info;
    }

    public static LoggerEvent create(InternalLogLevel level, String msg, Throwable e) {
        LoggerEvent info = new LoggerEvent();
        info.level = level;
        info.msg = msg;
        info.throwable = e;
        return info;
    }

    public InternalLogLevel getLevel() {
        return level;
    }

    public String getMsg() {
        return msg;
    }

    public Object[] getArgs() {
        return args;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public LoggerEvent setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public String getName() {
        return name;
    }

    public LoggerEvent setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("throwable", throwable);
        map.put("level", level.name());
        map.put("msg", msg);
        map.put("args", args);
        map.put("name", name);

        return map;
    }
}
