package org.beifengtz.jvmm.agent;

import java.util.Arrays;
import java.util.Map;

/**
 * Created in 10:10 2021/12/14
 *
 * @author beifengtz
 */
public class LoggerEvent {
    private String level;
    private String msg;
    private Object[] args;
    private Throwable throwable;
    private String name;

    public String getLevel() {
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

    public String getName() {
        return name;
    }


    public static LoggerEvent fromMap(Map<String, Object> map) {
        LoggerEvent event = new LoggerEvent();

        event.throwable = (Throwable) map.get("throwable");
        event.level = (String) map.get("level");
        event.msg = (String) map.get("msg");
        event.args = (Object[]) map.get("args");
        event.name = (String) map.get("name");

        return event;
    }

    @Override
    public String toString() {
        return "LoggerEvent{" +
                "level='" + level + '\'' +
                ", msg='" + msg + '\'' +
                ", args=" + Arrays.toString(args) +
                ", throwable=" + throwable +
                ", name='" + name + '\'' +
                '}';
    }
}
