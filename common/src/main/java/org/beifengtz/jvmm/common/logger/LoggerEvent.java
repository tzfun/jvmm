package org.beifengtz.jvmm.common.logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Description: TODO
 *
 * Created in 15:53 2021/12/9
 *
 * @author beifengtz
 */
public class LoggerEvent {

    private LoggerLevel type;
    private String msg;
    private Object[] args;
    private Throwable throwable;
    private String name;

    public static LoggerEvent create(LoggerLevel type, String msg) {
        LoggerEvent info = new LoggerEvent();
        info.type = type;
        info.msg = msg;
        return info;
    }

    public static LoggerEvent create(LoggerLevel type, String format, Object... args) {
        LoggerEvent info = new LoggerEvent();
        info.type = type;
        info.msg = format;
        info.args = args;
        return info;
    }

    public static LoggerEvent create(LoggerLevel type, String msg, Throwable e) {
        LoggerEvent info = new LoggerEvent();
        info.type = type;
        info.msg = msg;
        info.throwable = e;
        return info;
    }

    public LoggerLevel getType() {
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

    @Override
    public String toString() {
        Throwable t = this.throwable;
        try {
            setThrowable(null);
            JsonObject json = new JsonObject();
            if (type != null) {
                json.addProperty("type", type.name());
            }
            if (msg != null) {
                json.addProperty("msg", msg);
            }
            if (args != null) {
                JsonArray arr = new JsonArray(args.length);
                for (Object arg : args) {
                    arr.add(arg.toString());
                }
                json.add("args", arr);
            }
            if (name != null) {
                json.addProperty("name", name);
            }
            return json.toString();
        } finally {
            setThrowable(t);
        }
    }
}
