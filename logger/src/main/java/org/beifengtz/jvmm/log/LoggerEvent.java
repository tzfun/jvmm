package org.beifengtz.jvmm.log;

import io.netty.util.internal.logging.InternalLogLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: TODO
 *
 * Created in 15:53 2021/12/9
 *
 * @author beifengtz
 */
public class LoggerEvent {

    private InternalLogLevel type;
    private String msg;
    private Object[] args;
    private Throwable throwable;
    private String name;

    public static LoggerEvent create(InternalLogLevel type, String msg) {
        LoggerEvent info = new LoggerEvent();
        info.type = type;
        info.msg = msg;
        return info;
    }

    public static LoggerEvent create(InternalLogLevel type, String format, Object... args) {
        LoggerEvent info = new LoggerEvent();
        info.type = type;
        info.msg = format;
        info.args = args;
        return info;
    }

    public static LoggerEvent create(InternalLogLevel type, String msg, Throwable e) {
        LoggerEvent info = new LoggerEvent();
        info.type = type;
        info.msg = msg;
        info.throwable = e;
        return info;
    }

    public InternalLogLevel getType() {
        return type;
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
        map.put("type", type.name());
        map.put("msg", msg);
        map.put("args", args);
        map.put("name", name);

        return map;
    }
}
